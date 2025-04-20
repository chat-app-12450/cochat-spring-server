package com.sns.project.handler.exceptionHandler.exception.duplication;

public class DuplicatedMessageException extends RuntimeException {
    public DuplicatedMessageException(String message) {
        super(message);
    }
}
