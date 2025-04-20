package com.sns.project.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

import com.sns.project.service.user.TokenService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthAspect {

    private final TokenService tokenService;

    @Around("@annotation(com.sns.project.aspect.AuthRequired)")
    public Object validateToken(ProceedingJoinPoint joinPoint) throws Throwable {
        
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = extractTokenFromCookies(request);
        Long userId = tokenService.validateToken(token);
        log.info("userId: {}", userId);
        UserContext.setUserId(userId);
        
        try {
            return joinPoint.proceed();
        } finally {
            UserContext.clear();
        }
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
} 