package com.sns.project.handler.exceptionHandler.exception.badRequest;

public class InvalidFormatRequestException extends RuntimeException {
    public InvalidFormatRequestException(String message) {
        super(message);
    }

}
