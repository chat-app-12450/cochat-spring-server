package com.sns.project.repository.chat;

import com.sns.project.domain.chat.ChatMessage;
import com.sns.project.domain.chat.ChatRoom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Optional<ChatMessage> findByChatRoomAndClientMessageId(ChatRoom room, String clientMessageId);

    @Query("SELECT cm FROM ChatMessage cm JOIN FETCH cm.sender u "
        + "WHERE cm.chatRoom.id = :chatRoomId "
        + "ORDER BY cm.id ASC")
    List<ChatMessage> findByChatRoomIdWithUser(Long chatRoomId);


    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.id > :lastReadId")
    List<ChatMessage> findUnreadChatMessage(Long roomId, Long lastReadId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId ORDER BY cm.id DESC LIMIT 1")
    ChatMessage findLastMessage(Long roomId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId")
    List<ChatMessage> findAllByRoomId(Long roomId);
}
