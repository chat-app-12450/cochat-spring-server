package com.sns.project.repository.notification;

import com.sns.project.domain.notification.Notification;
import com.sns.project.domain.user.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n.id FROM Notification n WHERE n.receiver.id = :receiverId ORDER BY n.createdAt DESC")
    Page<Long> findNotificationIds(@Param("receiverId") Long receiverId, Pageable pageable);
    
    @Query("SELECT n FROM Notification n JOIN FETCH n.notificationContent WHERE n.id IN :notificationIds")
    List<Notification> findNotificationsWithDetails(@Param("notificationIds") List<Long> notificationIds);

    Optional<Notification> findByIdAndReceiver(Long id, User receiver);

} 