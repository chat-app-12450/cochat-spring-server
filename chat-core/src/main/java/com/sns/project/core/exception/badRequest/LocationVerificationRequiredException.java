package com.sns.project.core.exception.badRequest;

public class LocationVerificationRequiredException extends RuntimeException {

    public LocationVerificationRequiredException(String message) {
        super(message);
    }
}
