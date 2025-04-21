package com.sns.project.core.exception.duplication;

public class DuplicatedMessageException extends RuntimeException {
    public DuplicatedMessageException(String message) {
        super(message);
    }
}
