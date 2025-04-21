package com.sns.project.core.repository.chat;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sns.project.core.domain.chat.ChatReadStatus;

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
    Optional<ChatReadStatus> findByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);
} 