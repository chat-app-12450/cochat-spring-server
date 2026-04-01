package com.sns.project.auth;

import com.sns.project.auth.AuthCookieService;
import com.sns.project.core.exception.unauthorized.UnauthorizedException;
import lombok.RequiredArgsConstructor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import com.sns.project.auth.TokenService;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthAspect {

    private final TokenService tokenService;
    private final AuthCookieService authCookieService;

    @Around("@annotation(com.sns.project.auth.AuthRequired)")
    public Object validateToken(ProceedingJoinPoint joinPoint) throws Throwable {
        
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = authCookieService.extractAccessToken(request)
            .orElseThrow(() -> new UnauthorizedException("인증 토큰이 없습니다."));
        Long userId = tokenService.validateToken(token);
        UserContext.setUserId(userId);
        
        try {
            return joinPoint.proceed();
        } finally {
            UserContext.clear();
        }
    }
} 
