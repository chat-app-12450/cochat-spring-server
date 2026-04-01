package com.sns.project.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class CsrfProtectionFilter extends OncePerRequestFilter {

    private final AuthCookieService authCookieService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // 조회성 요청은 서버 상태를 바꾸지 않으므로 CSRF 검사 대상에서 제외한다.
        return HttpMethod.GET.matches(request.getMethod())
            || HttpMethod.HEAD.matches(request.getMethod())
            || HttpMethod.OPTIONS.matches(request.getMethod())
            // 로그인/회원가입/토큰확인은 인증 상태를 만들기 전 단계이거나 조회성 성격이라
            // 오래된 쿠키가 남아 있어도 CSRF 검사 때문에 막히지 않게 예외 처리한다.
            || "/api/user/login".equals(requestUri)
            || "/api/user/register".equals(requestUri)
            || "/api/user/validate-token".equals(requestUri);
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = authCookieService.extractAccessToken(request).orElse(null);
        String refreshToken = authCookieService.extractRefreshToken(request).orElse(null);
        if (!StringUtils.hasText(accessToken) && !StringUtils.hasText(refreshToken)) {
            // 로그인하지 않은 요청은 인증 쿠키가 없으므로 CSRF 검사 없이 다음 필터로 넘긴다.
            filterChain.doFilter(request, response);
            return;
        }

        // 로그인된 브라우저가 보낸 변경 요청이라면
        // 1) 브라우저가 자동으로 싣는 CSRF 쿠키와
        // 2) 프론트 JS가 직접 복사해서 넣은 헤더 값을 비교한다.
        String csrfCookie = authCookieService.extractCsrfToken(request).orElse(null);
        String csrfHeader = request.getHeader(authCookieService.getCsrfHeaderName());
        if (!isValidCsrf(csrfCookie, csrfHeader)) {
            writeForbidden(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidCsrf(String csrfCookie, String csrfHeader) {
        // 두 위치의 값이 모두 있어야 하고, 정확히 일치해야만 정상 요청으로 본다.
        if (!StringUtils.hasText(csrfCookie) || !StringUtils.hasText(csrfHeader)) {
            return false;
        }

        return MessageDigest.isEqual(
            csrfCookie.getBytes(StandardCharsets.UTF_8),
            csrfHeader.getBytes(StandardCharsets.UTF_8)
        );
    }

    private void writeForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
            objectMapper.writeValueAsString(ApiResult.error("CSRF token validation failed", HttpStatus.FORBIDDEN))
        );
    }
}
