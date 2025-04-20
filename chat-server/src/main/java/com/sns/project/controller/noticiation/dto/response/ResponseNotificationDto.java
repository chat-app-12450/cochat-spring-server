package com.sns.project.controller.noticiation.dto.response;

import java.time.LocalDateTime;

import com.sns.project.domain.notification.Notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponseNotificationDto {
    
    private Long id;
    private String message;
    private LocalDateTime createdAt;
    public ResponseNotificationDto(Notification notification) {
        this.id = notification.getId();
        this.message = notification.getNotificationContent().getMessage();
        this.createdAt = notification.getCreatedAt();
    }
}
