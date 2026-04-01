package com.sns.project.auth;

import com.sns.project.core.exception.unauthorized.UnauthorizedException;
import com.sns.project.service.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/*
 * 인증 토큰 저장/검증 규칙
 *
 * 1. login
 * - access token: 짧게 발급하고 서버 저장 없이 stateless 로 검증한다.
 * - refresh token: 길게 발급하고 Redis 세션 저장소에 함께 등록한다.
 *
 * 2. refresh
 * - 클라이언트가 refresh token을 보내면 JWT 서명/만료/type을 먼저 확인한다.
 * - 그 다음 refresh token 안의 jti 로 Redis 세션 키를 조회한다.
 * - Redis 에 세션이 있으면 새 access/refresh 를 다시 발급하고, 이전 refresh 세션은 지운다.
 *
 * 3. logout
 * - refresh 세션만 Redis 에서 제거한다.
 * - access token 은 stateless 이므로 서버가 즉시 폐기하지 않고 짧은 TTL 후 자연 만료시킨다.
 */
public class TokenService {

    private static final String USER_ID_CLAIM = "userId";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String REFRESH_SESSION_PREFIX = "auth:refresh:";

    private final RedisService redisService;

    @Value("${jwt.secret}")
    private  String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationTime;

    public Long validateToken(String token) {
        return validateAccessToken(token);
    }


    public Long validateAccessToken(String token) {
        Claims claims = parseAndValidateType(token, ACCESS_TOKEN_TYPE);
        return extractUserId(claims);
    }

    public Long validateRefreshToken(String token) {
        Claims claims = parseAndValidateType(token, REFRESH_TOKEN_TYPE);
        validateRefreshSession(claims);
        return extractUserId(claims);
    }

    public TokenPair issueTokens(Long userId) {
        String accessToken = generateAccessToken(userId);
        IssuedToken refreshToken = generateRefreshToken(userId);
        storeRefreshSession(refreshToken.jti(), userId, refreshToken.expiresAt());
        return new TokenPair(accessToken, refreshToken.value());
    }

    public void revokeRefreshToken(String token) {
        Claims claims = parseAndValidateType(token, REFRESH_TOKEN_TYPE);
        deleteRefreshSession(claims.getId());
    }

    public void revokeRefreshTokenSilently(String token) {
        try {
            revokeRefreshToken(token);
        } catch (RuntimeException ignored) {
            // 로그아웃 경로에서는 이미 만료되었거나 손상된 토큰 때문에 실패시키지 않는다.
        }
    }

    private Claims parseAndValidateType(String token, String expectedType) {
        Claims claims = parseClaims(token);
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        if (!expectedType.equals(tokenType)) {
            throw new UnauthorizedException("유효하지 않은 토큰 타입입니다.");
        }

        return claims;
    }

    private Claims parseClaims(String token) {
        try {
            Key key = signingKey();
            return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("토큰이 만료되었습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }
    }

    private Key signingKey() {
        // access token은 stateless, refresh token은 Redis 세션 저장소를 통해 관리한다.
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    private Long extractUserId(Claims claims) {
        return Long.valueOf(claims.get(USER_ID_CLAIM).toString());
    }

    private void validateRefreshSession(Claims claims) {
        // refresh token 은 서버가 Redis 에 등록해 둔 세션과 매칭되어야만 재발급에 사용할 수 있다.
        String jti = claims.getId();
        if (jti == null) {
            throw new UnauthorizedException("유효하지 않은 refresh token입니다.");
        }

        Long userId = extractUserId(claims);
        // Redis 키: auth:refresh:{jti}
        // Redis 값: userId
        // 이유:
        // - jti 는 "이 refresh 토큰 세션 하나"를 식별한다.
        // - value 로 userId 를 저장해 두면 토큰 안의 userId 와 Redis 세션 주인이 같은지도 확인할 수 있다.
        Long storedUserId = redisService.getValue(refreshSessionKey(jti), Long.class)
            .orElseThrow(() -> new UnauthorizedException("만료되었거나 폐기된 refresh token입니다."));

        if (!userId.equals(storedUserId)) {
            throw new UnauthorizedException("유효하지 않은 refresh token입니다.");
        }
    }

    private void storeRefreshSession(String jti, Long userId, Instant expiresAt) {
        // refresh token 을 발급할 때만 Redis 에 세션을 저장한다.
        // access token 은 서버 저장 없이 stateless 로만 쓴다.
        long ttlSeconds = Math.max(1, expiresAt.getEpochSecond() - Instant.now().getEpochSecond());
        redisService.setValueWithExpiration(refreshSessionKey(jti), userId, ttlSeconds);
    }

    private void deleteRefreshSession(String jti) {
        if (jti != null) {
            redisService.deleteValue(refreshSessionKey(jti));
        }
    }

    private String refreshSessionKey(String jti) {
        return REFRESH_SESSION_PREFIX + jti;
    }

    private String generateAccessToken(Long userId) {
        // access token 은 매 요청 인증용이다.
        // 지금 구조에서는 Redis 조회를 하지 않으므로 jti 를 넣지 않는다.
        Key key = signingKey();
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + expirationTime);

        return Jwts.builder()
            .claim(USER_ID_CLAIM, userId)
            .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
            .setIssuedAt(issuedAt)
            .setExpiration(expiresAt)
            .signWith(key)
            .compact();
    }

    private IssuedToken generateRefreshToken(Long userId) {
        // refresh token 은 Redis 세션 저장소에서 찾을 식별자가 필요하므로 jti 를 반드시 넣는다.
        Key key = signingKey();
        long now = System.currentTimeMillis();
        long expiry = now + refreshExpirationTime;
        String jti = UUID.randomUUID().toString();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(expiry);

        String token = Jwts.builder()
            .claim(USER_ID_CLAIM, userId)
            .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
            .setId(jti)
            .setIssuedAt(issuedAt)
            .setExpiration(expiresAt)
            .signWith(key)
            .compact();

        return new IssuedToken(token, jti, expiresAt.toInstant());
    }

    public record TokenPair(String accessToken, String refreshToken) {
    }

    private record IssuedToken(String value, String jti, Instant expiresAt) {
    }
}
