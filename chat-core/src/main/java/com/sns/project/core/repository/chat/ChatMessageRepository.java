package com.sns.project.core.repository.chat;

import com.sns.project.core.domain.chat.ChatMessage;
import com.sns.project.core.domain.chat.ChatRoom;
import java.util.List;
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

    // 히스토리 한 페이지에 포함된 메시지들에 대해서만 unreadCount를 DB에서 집계한다.
    // sender는 제외하고, 각 참가자의 lastReadSeq가 messageSeq보다 작은 경우만 unread로 센다.
    @Query("""
        SELECT cm.id AS messageId, COUNT(cp.id) AS unreadCount
        FROM ChatMessage cm
        JOIN cm.chatRoom.participants cp
        LEFT JOIN ChatReadStatus cr
            ON cr.chatRoom = cm.chatRoom
           AND cr.user = cp.user
        WHERE cm.id IN :messageIds
          AND cp.user.id <> cm.sender.id
          AND (cr.lastReadSeq IS NULL OR cr.lastReadSeq < cm.messageSeq)
        GROUP BY cm.id
        """)
    List<MessageUnreadCountProjection> countUnreadParticipantsByMessageIds(
        @Param("messageIds") List<Long> messageIds
    );

    // Redis unread 캐시가 비었을 때만 쓰는 복구용 COUNT 쿼리다.
    // 평소 요청마다 unread를 다시 세면 비용이 커지므로,
    // roomIds에 포함된 방들에 한해서 현재 사용자가 "안 읽은 남의 메시지 수"만 방별로 재계산한다.
    @Query("""
        SELECT cm.chatRoom.id AS roomId, COUNT(cm.id) AS unreadCount
        FROM ChatMessage cm
        LEFT JOIN ChatReadStatus cr
            ON cr.chatRoom = cm.chatRoom
           AND cr.user.id = :userId
        WHERE cm.chatRoom.id IN :roomIds
          AND cm.sender.id <> :userId
          AND (cr.lastReadSeq IS NULL OR cm.messageSeq > cr.lastReadSeq)
        GROUP BY cm.chatRoom.id
        """)
    List<RoomUnreadCountProjection> countUnreadMessagesByRoomIds(
        @Param("userId") Long userId,
        @Param("roomIds") List<Long> roomIds
    );
} 
