package com.sns.project.repository.chat;

import com.sns.project.domain.chat.ChatParticipant;
import com.sns.project.domain.chat.ChatRoom;
import com.sns.project.domain.user.User;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);
    List<ChatParticipant> findByUser(User user);
    boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);
    
    @Query("SELECT cp.chatRoom FROM ChatParticipant cp WHERE cp.user.id = :userId")
    List<ChatRoom> findChatRoomsByUserId(@Param("userId") Long userId);

    @Query("SELECT cp.user.id FROM ChatParticipant cp WHERE cp.chatRoom.id = :chatRoomId")
    Set<Long> findUserIdsByChatRoomId(@Param("chatRoomId") Long chatRoomId);
    Long countByChatRoomId(Long roomId);

    @Query("SELECT cp.user.id FROM ChatParticipant cp WHERE cp.chatRoom.id = :chatRoomId")
    List<Long> findParticipantIdsByRoomId(@Param("chatRoomId") Long chatRoomId);
}
