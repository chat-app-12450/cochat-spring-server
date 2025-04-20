package com.sns.project.service;

import com.sns.project.domain.notification.Notification;
import com.sns.project.domain.notification.NotificationContent;
import com.sns.project.domain.user.User;
import com.sns.project.controller.noticiation.dto.workerDto.RawNotificationDto;
import com.sns.project.handler.exceptionHandler.exception.notfound.NotFoundNotificationException;
import com.sns.project.repository.notification.NotificationContentRepository;
import com.sns.project.repository.notification.NotificationRepository;
import com.sns.project.service.user.UserService;
import com.sns.project.worker.notification.NotificationSplitWorker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationContentRepository contentRepository;
    private final NotificationSplitWorker splitWorker;
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    
    public void sendNotification(String message, Long senderId, List<Long> recipientIds) {
        NotificationContent content = createAndSaveNotificationContent(message);
        RawNotificationDto rawDto = buildRawNotificationDto(content.getId(), senderId, recipientIds);

        splitWorker.enqueue(rawDto);
        log.info("Enqueued notification for sender: {} with contentId: {}", rawDto.getSenderId(), rawDto.getContentId());
    }

    private NotificationContent createAndSaveNotificationContent(String message) {
        NotificationContent content = new NotificationContent(message);
        return contentRepository.save(content);
    }

    private RawNotificationDto buildRawNotificationDto(Long contentId, Long senderId, List<Long> recipientIds) {
        return RawNotificationDto.builder()
                .contentId(contentId)
                .senderId(senderId)
                .recipientIds(recipientIds)
                .build();
    }

    public Page<Notification> getNotifications(Long receiverId, int page, int size) {
        userService.isExistUser(receiverId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Long> notificationIdsPage = notificationRepository.findNotificationIds(receiverId, pageable);
        List<Notification> notifications = fetchNotificationsWithDetails(notificationIdsPage.getContent());

        return new PageImpl<>(notifications, pageable, notificationIdsPage.getTotalElements());
    }

    private List<Notification> fetchNotificationsWithDetails(List<Long> notificationIds) {
        return notificationRepository.findNotificationsWithDetails(notificationIds);
    }

    public void deleteNotification(Long notificationId, Long userId) {
        User receiver = userService.getUserById(userId);
        Notification notification = findNotification(notificationId, receiver);

        notificationRepository.delete(notification);
        log.info("Deleted notification with ID: {} for user: {}", notificationId, userId);
    }

    private Notification findNotification(Long notificationId, User receiver) {
        return notificationRepository.findByIdAndReceiver(notificationId, receiver)
                .orElseThrow(() -> new NotFoundNotificationException(notificationId));
    }
}