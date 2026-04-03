package com.sns.project.core.repository.chat;

import com.sns.project.core.domain.chat.ChatParticipant;
import com.sns.project.core.domain.chat.ChatRoom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  // DISTINCT:
  // - participants fetch join 때문에 채팅방이 참가자 수만큼 중복 row 로 늘어나는 것을 정리한다.
  // EXISTS:
  // - fetch join 한 participant row 자체를 거르지 않고,
  //   "이 채팅방에 현재 사용자가 참여하고 있는가"만 별도로 검사한다.
  @Query("""
      SELECT DISTINCT c
      FROM ChatRoom c
      JOIN FETCH c.participants p
      LEFT JOIN FETCH c.product product
      JOIN FETCH p.user
      WHERE EXISTS (
        SELECT 1
        FROM ChatParticipant cp
        WHERE cp.chatRoom = c
          AND cp.user.id = :userId
          AND cp.leaveSeq IS NULL
      )
        AND c.openChat = false
      ORDER BY
        CASE WHEN c.latestMessageAt IS NULL THEN 1 ELSE 0 END,
        c.latestMessageAt DESC,
        c.id DESC
      """)
  List<ChatRoom> findChatRoomsWithParticipantsByUserId(@Param("userId") Long userId);

  @Query("""
      SELECT DISTINCT c
      FROM ChatRoom c
      JOIN FETCH c.participants p
      JOIN FETCH p.user
      LEFT JOIN FETCH c.product product
      WHERE c.chatRoomType = com.sns.project.core.domain.chat.ChatRoomType.PRIVATE
        AND product.id = :productId
        AND EXISTS (
          SELECT 1 FROM ChatParticipant cp
          WHERE cp.chatRoom = c AND cp.user.id = :sellerId AND cp.leaveSeq IS NULL
        )
        AND EXISTS (
          SELECT 1 FROM ChatParticipant cp
          WHERE cp.chatRoom = c AND cp.user.id = :buyerId AND cp.leaveSeq IS NULL
        )
        AND (
          SELECT COUNT(cp)
          FROM ChatParticipant cp
          WHERE cp.chatRoom = c
            AND cp.leaveSeq IS NULL
        ) = 2
      """)
  Optional<ChatRoom> findPrivateProductRoomByProductIdAndParticipantIds(
      @Param("productId") Long productId,
      @Param("sellerId") Long sellerId,
      @Param("buyerId") Long buyerId);

  @Query("""
      SELECT DISTINCT c
      FROM ChatRoom c
      LEFT JOIN FETCH c.participants p
      LEFT JOIN FETCH p.user
      LEFT JOIN FETCH c.product product
      WHERE c.id = :roomId
      """)
  Optional<ChatRoom> findByIdWithParticipants(@Param("roomId") Long roomId);

  @Query("""
      SELECT DISTINCT c
      FROM ChatRoom c
      LEFT JOIN FETCH c.participants p
      LEFT JOIN FETCH p.user
      LEFT JOIN FETCH c.product product
      WHERE c.chatRoomType = com.sns.project.core.domain.chat.ChatRoomType.GROUP
        AND c.openChat = true
        AND (:keyword IS NULL OR :keyword = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
      ORDER BY
        CASE WHEN c.latestMessageAt IS NULL THEN 1 ELSE 0 END,
        c.latestMessageAt DESC,
        c.id DESC
      """)
  List<ChatRoom> searchOpenGroupRooms(@Param("keyword") String keyword);

  @Query("""
      SELECT DISTINCT c
      FROM ChatRoom c
      JOIN FETCH c.participants p
      JOIN FETCH p.user
      LEFT JOIN FETCH c.product product
      WHERE c.openChat = true
        AND EXISTS (
          SELECT 1
          FROM ChatParticipant cp
          WHERE cp.chatRoom = c
            AND cp.user.id = :userId
            AND cp.leaveSeq IS NULL
      )
      ORDER BY
        CASE WHEN c.latestMessageAt IS NULL THEN 1 ELSE 0 END,
        c.latestMessageAt DESC,
        c.id DESC
      """)
  List<ChatRoom> findJoinedOpenChatRoomsByUserId(@Param("userId") Long userId);

  // @Query("SELECT c FROM ChatRoom c WHERE c.id = :roomId")
  // Optional<ChatRoom> findById(@Param("roomId") Long roomId);
}
