package com.sns.project.core.repository.chat;

import com.sns.project.core.domain.chat.ChatMessage;
import com.sns.project.core.domain.chat.ChatRoom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
        SELECT cm
        FROM ChatMessage cm
        JOIN FETCH cm.sender sender
        WHERE cm.id IN :messageIds
        """)
    List<ChatMessage> findAllWithSenderByIdIn(@Param("messageIds") List<Long> messageIds);

    @Query("""
        SELECT cm
        FROM ChatMessage cm
        JOIN FETCH cm.sender sender
        WHERE cm.chatRoom.id = :roomId
        ORDER BY cm.id DESC
        """)
    List<ChatMessage> findRecentMessagesWithSender(
        @Param("roomId") Long roomId,
        Pageable pageable);

    @Query("""
        SELECT cm
        FROM ChatMessage cm
        JOIN FETCH cm.sender sender
        WHERE cm.chatRoom.id = :roomId
          AND cm.id < :beforeMessageId
        ORDER BY cm.id DESC
        """)
    List<ChatMessage> findMessagesWithSenderBeforeId(
        @Param("roomId") Long roomId,
        @Param("beforeMessageId") Long beforeMessageId,
        Pageable pageable);

    @Query("SELECT cm FROM ChatMessage cm JOIN FETCH cm.sender u "
        + "WHERE cm.chatRoom.id = :chatRoomId "
        + "ORDER BY cm.id ASC")
    List<ChatMessage> findByChatRoomIdWithUser(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.id > :lastReadId")
    List<ChatMessage> findUnreadChatMessage(@Param("roomId") Long roomId, @Param("lastReadId") Long lastReadId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId ORDER BY cm.id DESC LIMIT 1")
    ChatMessage findLastMessage(@Param("roomId") Long roomId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId")
    List<ChatMessage> findAllByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId ORDER BY cm.id ASC")
    List<ChatMessage> findByChatRoomId(@Param("roomId") Long roomId);
} 
