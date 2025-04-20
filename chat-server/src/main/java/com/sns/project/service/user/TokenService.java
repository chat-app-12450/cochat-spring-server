package com.sns.project.service.user;

import com.sns.project.handler.exceptionHandler.exception.unauthorized.UnauthorizedException;
import com.sns.project.service.RedisService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisService redisService;

    public Long validateToken(String token) {

        if (token == null) {
            throw new UnauthorizedException("토큰이 필요합니다.");
        }

        Optional<Long> userIdOpt = getUserId(token);
        if (userIdOpt.isEmpty()) {
            throw new UnauthorizedException(token+" : 유효하지 않은 토큰입니다.");
        }

        return userIdOpt.get();
    }


    public boolean isValidToken(String token) {
        if (token == null) {
            return false;
        }
        Optional<Long> userIdOpt = getUserId(token);
        return userIdOpt.isPresent();
    }

    private Optional<Long> getUserId(String token) {
        Optional<Long> userIdOpt = redisService.getValue(token, Long.class);
        return userIdOpt;
    }
}