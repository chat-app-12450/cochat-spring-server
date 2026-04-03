package com.sns.project.core.repository.chat;

import com.sns.project.core.domain.chat.ChatParticipant;
import com.sns.project.core.domain.chat.ChatRoom;
import com.sns.project.core.domain.user.User;
import java.util.Set;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);
    List<ChatParticipant> findByUser(User user);
    boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);
    boolean existsByChatRoomIdAndUserIdAndLeaveSeqIsNull(Long roomId, Long userId);
    Optional<ChatParticipant> findTopByChatRoomIdAndUserIdAndLeaveSeqIsNullOrderByIdDesc(Long roomId, Long userId);
    
    @Query("SELECT cp.chatRoom FROM ChatParticipant cp WHERE cp.user.id = :userId AND cp.leaveSeq IS NULL")
    List<ChatRoom> findChatRoomsByUserId(@Param("userId") Long userId);

    @Query("SELECT cp.user.id FROM ChatParticipant cp WHERE cp.chatRoom.id = :chatRoomId AND cp.leaveSeq IS NULL")
    Set<Long> findUserIdsByChatRoomId(@Param("chatRoomId") Long chatRoomId);
    Long countByChatRoomIdAndLeaveSeqIsNull(Long roomId);

    @Query("SELECT cp.user.id FROM ChatParticipant cp WHERE cp.chatRoom.id = :chatRoomId AND cp.leaveSeq IS NULL")
    List<Long> findActiveParticipantIdsByRoomId(@Param("chatRoomId") Long chatRoomId);
}
