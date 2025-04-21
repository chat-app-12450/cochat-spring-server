package com.sns.project.core.repository.chat;

import com.sns.project.core.domain.chat.ChatParticipant;
import com.sns.project.core.domain.chat.ChatRoom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  @Query("SELECT DISTINCT c FROM ChatRoom c " +
      "JOIN FETCH c.participants p " +
      "JOIN FETCH p.user " +
      "WHERE EXISTS (SELECT 1 FROM ChatParticipant cp WHERE cp.chatRoom = c AND cp.user.id = :userId)")
  List<ChatRoom> findChatRoomsWithParticipantsByUserId(@Param("userId") Long userId);

  // @Query("SELECT c FROM ChatRoom c WHERE c.id = :roomId")
  // Optional<ChatRoom> findById(@Param("roomId") Long roomId);
} 