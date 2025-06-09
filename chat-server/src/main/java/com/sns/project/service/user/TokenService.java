package com.sns.project.service.user;

import com.sns.project.core.exception.unauthorized.UnauthorizedException;
import com.sns.project.service.RedisService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisService redisService;
    private final String secretKey = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfgh";

    public Long validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
            

            return Long.valueOf(claims.get("userId").toString());
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("토큰이 만료되었습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }
    }

    public String generateToken(Long userId) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        long expiry = now + 1000 * 60 * 60; // 1시간

        return Jwts.builder()
            .claim("userId", userId)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(expiry))
            .signWith(key)
            .compact();
    }
}