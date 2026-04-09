package com.sns.project.core.repository.chat;

import com.sns.project.core.domain.chat.ChatMessage;
import com.sns.project.core.domain.chat.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
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
        ORDER BY cm.messageSeq DESC
        """)
    List<ChatMessage> findRecentMessagesWithSender(
        @Param("roomId") Long roomId,
        Pageable pageable);

    @Query("""
        SELECT cm
        FROM ChatMessage cm
        JOIN FETCH cm.sender sender
        WHERE cm.chatRoom.id = :roomId
          AND cm.messageSeq < :beforeMessageSeq
        ORDER BY cm.messageSeq DESC
        """)
    List<ChatMessage> findMessagesWithSenderBeforeMessageSeq(
        @Param("roomId") Long roomId,
        @Param("beforeMessageSeq") Long beforeMessageSeq,
        Pageable pageable);

    @Query("SELECT cm FROM ChatMessage cm JOIN FETCH cm.sender u "
        + "WHERE cm.chatRoom.id = :chatRoomId "
        + "ORDER BY cm.messageSeq ASC")
    List<ChatMessage> findByChatRoomIdWithUser(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.messageSeq > :lastReadSeq")
    List<ChatMessage> findUnreadChatMessage(@Param("roomId") Long roomId, @Param("lastReadSeq") Long lastReadSeq);

    ChatMessage findTopByChatRoomIdOrderByMessageSeqDesc(Long roomId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId")
    List<ChatMessage> findAllByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId ORDER BY cm.messageSeq ASC")
    List<ChatMessage> findByChatRoomId(@Param("roomId") Long roomId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE ChatMessage cm
        SET cm.unreadCount = cm.unreadCount - 1
        WHERE cm.chatRoom.id = :roomId
          AND cm.messageSeq > :previousReadSeq
          AND cm.messageSeq <= :newReadSeq
          AND cm.sender.id <> :readerId
          AND cm.unreadCount > 0
        """)
    int decrementUnreadCountInRange(
        @Param("roomId") Long roomId,
        @Param("readerId") Long readerId,
        @Param("previousReadSeq") Long previousReadSeq,
        @Param("newReadSeq") Long newReadSeq
    );

    // Redis unread 캐시가 비었을 때만 쓰는 복구용 COUNT 쿼리다.
    // 평소 요청마다 unread를 다시 세면 비용이 커지므로,
    // roomIds에 포함된 방들에 한해서 현재 사용자가 "안 읽은 남의 메시지 수"만 방별로 재계산한다.
    @Query("""
        SELECT cm.chatRoom.id AS roomId, COUNT(cm.id) AS unreadCount
        FROM ChatMessage cm
        JOIN ChatParticipant cp
            ON cp.chatRoom = cm.chatRoom
           AND cp.user.id = :userId
           AND cp.leaveSeq IS NULL
        WHERE cm.chatRoom.id IN :roomIds
          AND cp.joinSeq <= cm.messageSeq
          AND cm.sender.id <> :userId
          AND cm.messageSeq > cp.lastReadSeq
        GROUP BY cm.chatRoom.id
        """)
    List<RoomUnreadCountProjection> countUnreadMessagesByRoomIds(
        @Param("userId") Long userId,
        @Param("roomIds") List<Long> roomIds
    );
} 
