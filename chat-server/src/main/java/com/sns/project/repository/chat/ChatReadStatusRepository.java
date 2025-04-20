package com.sns.project.repository.chat;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.sns.project.domain.chat.ChatReadStatus;

import io.lettuce.core.dynamic.annotation.Param;


public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus, Long> {
    Optional<ChatReadStatus> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);
    
    @Modifying
    @Query("""
        UPDATE ChatReadStatus cr
        SET cr.lastReadMessageId = :newMessageId,
            cr.updatedAt = CURRENT_TIMESTAMP
        WHERE cr.user.id = :userId
          AND cr.chatRoom.id = :roomId
          AND (cr.lastReadMessageId IS NULL OR cr.lastReadMessageId < :newMessageId)
    """)
    int updateIfLastReadIdIsSmaller(
        @Param("userId") Long userId,
        @Param("roomId") Long roomId,
        @Param("newMessageId") Long newMessageId
    );

    @Query("SELECT cr FROM ChatReadStatus cr where cr.user.id = :userId and cr.chatRoom.id = :roomId")
    Optional<ChatReadStatus> findByUserIdAndRoomId(Long userId, Long roomId);
}
