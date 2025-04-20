package com.sns.project.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sns.project.domain.notification.NotificationContent;

public interface NotificationContentRepository extends JpaRepository<NotificationContent, Long> {
    
}
