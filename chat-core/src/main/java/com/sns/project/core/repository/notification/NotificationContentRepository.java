package com.sns.project.core.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sns.project.core.domain.notification.NotificationContent;

@Repository
public interface NotificationContentRepository extends JpaRepository<NotificationContent, Long> {
    
} 