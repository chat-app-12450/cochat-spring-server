package com.sns.project.handler.exceptionHandler.exception.unauthorized;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
} 