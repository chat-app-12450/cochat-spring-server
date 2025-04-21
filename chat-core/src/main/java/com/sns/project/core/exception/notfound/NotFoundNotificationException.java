package com.sns.project.core.exception.notfound;

public class NotFoundNotificationException extends RuntimeException {
    public NotFoundNotificationException(Long notificationId) {
        super("Notification with ID " + notificationId + " not found");
    }
}


