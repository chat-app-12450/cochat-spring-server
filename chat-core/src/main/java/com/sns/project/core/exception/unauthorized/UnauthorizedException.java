package com.sns.project.core.exception.unauthorized;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
} 