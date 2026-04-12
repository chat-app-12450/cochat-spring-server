package com.sns.project.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthCookieService {

    public static final String CSRF_HEADER_NAME = "X-CSRF-Token";

    private final AuthCookieProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public void writeAuthenticationCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        expireLegacyHostOnlyCookies(response);
        // ACCESS_TOKEN은 HttpOnly로 내려 JS에서 직접 읽지 못하게 한다.
        addCookie(response, buildAccessTokenCookie(accessToken, properties.getMaxAgeSeconds()));
        // REFRESH_TOKEN은 서버 재발급 전용으로 쓰며 access token보다 더 오래 유지한다.
        addCookie(response, buildRefreshTokenCookie(refreshToken, properties.getRefreshMaxAgeSeconds()));
        // XSRF-TOKEN은 프론트가 읽어 헤더로 다시 보내는 double-submit CSRF 용도다.
        addCookie(response, buildCsrfCookie(generateCsrfToken(), properties.getMaxAgeSeconds()));
    }

    public void clearAuthenticationCookies(HttpServletResponse response) {
        // 로그아웃 시 인증/CSRF 쿠키를 함께 만료시켜 세션 흔적을 정리한다.
        addCookie(response, buildAccessTokenCookie("", 0));
        addCookie(response, buildRefreshTokenCookie("", 0));
        addCookie(response, buildCsrfCookie("", 0));
        expireLegacyHostOnlyCookies(response);
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return extractCookieValue(request, properties.getAccessTokenName());
    }

    public Optional<String> extractCsrfToken(HttpServletRequest request) {
        return extractCookieValue(request, properties.getCsrfTokenName());
    }

    public boolean matchesAnyCsrfToken(HttpServletRequest request, String csrfHeader) {
        if (!StringUtils.hasText(csrfHeader) || request.getCookies() == null) {
            return false;
        }

        for (Cookie cookie : request.getCookies()) {
            if (!properties.getCsrfTokenName().equals(cookie.getName()) || !StringUtils.hasText(cookie.getValue())) {
                continue;
            }

            if (MessageDigest.isEqual(
                cookie.getValue().getBytes(StandardCharsets.UTF_8),
                csrfHeader.getBytes(StandardCharsets.UTF_8)
            )) {
                return true;
            }
        }

        return false;
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return extractCookieValue(request, properties.getRefreshTokenName());
    }

    public String getCsrfHeaderName() {
        return CSRF_HEADER_NAME;
    }

    private Optional<String> extractCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private void addCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void expireLegacyHostOnlyCookies(HttpServletResponse response) {
        if (!StringUtils.hasText(properties.getDomain())) {
            return;
        }

        addCookie(response, buildAccessTokenCookie("", 0, false));
        addCookie(response, buildRefreshTokenCookie("", 0, false));
        addCookie(response, buildCsrfCookie("", 0, false));
    }

    private String generateCsrfToken() {
        // 예측 가능한 값이면 CSRF 검증 의미가 없어지므로 충분히 긴 랜덤값을 쓴다.
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private ResponseCookie buildAccessTokenCookie(String token, long maxAgeSeconds) {
        return buildAccessTokenCookie(token, maxAgeSeconds, true);
    }

    private ResponseCookie buildAccessTokenCookie(String token, long maxAgeSeconds, boolean useConfiguredDomain) {
        // 실제 인증 토큰은 JS가 읽을 필요가 없으므로 HttpOnly 쿠키로만 전달한다.
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.getAccessTokenName(), token)
            .httpOnly(properties.isHttpOnly())
            .secure(properties.isSecure())
            .sameSite(properties.getSameSite())
            .path(properties.getPath())
            .maxAge(maxAgeSeconds);

        if (useConfiguredDomain && StringUtils.hasText(properties.getDomain())) {
            builder.domain(properties.getDomain());
        }

        return builder.build();
    }

    private ResponseCookie buildRefreshTokenCookie(String token, long maxAgeSeconds) {
        return buildRefreshTokenCookie(token, maxAgeSeconds, true);
    }

    private ResponseCookie buildRefreshTokenCookie(String token, long maxAgeSeconds, boolean useConfiguredDomain) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.getRefreshTokenName(), token)
            .httpOnly(properties.isHttpOnly())
            .secure(properties.isSecure())
            .sameSite(properties.getSameSite())
            .path(properties.getPath())
            .maxAge(maxAgeSeconds);

        if (useConfiguredDomain && StringUtils.hasText(properties.getDomain())) {
            builder.domain(properties.getDomain());
        }

        return builder.build();
    }

    private ResponseCookie buildCsrfCookie(String token, long maxAgeSeconds) {
        return buildCsrfCookie(token, maxAgeSeconds, true);
    }

    private ResponseCookie buildCsrfCookie(String token, long maxAgeSeconds, boolean useConfiguredDomain) {
        // CSRF 토큰은 프론트가 헤더로 복사해야 하므로 HttpOnly를 일부러 끈다.
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.getCsrfTokenName(), token)
            .httpOnly(false)
            .secure(properties.isSecure())
            .sameSite(properties.getSameSite())
            .path(properties.getPath())
            .maxAge(maxAgeSeconds);

        if (useConfiguredDomain && StringUtils.hasText(properties.getDomain())) {
            builder.domain(properties.getDomain());
        }

        return builder.build();
    }
}
