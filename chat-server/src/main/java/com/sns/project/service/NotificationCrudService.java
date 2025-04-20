package com.sns.project.service;

import com.sns.project.domain.notification.NotificationContent;
import com.sns.project.repository.notification.NotificationContentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class NotificationCrudService {
    private final NotificationContentRepository contentRepository;
    public NotificationContent findContentById(Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid notification content ID"));
    }    
}
