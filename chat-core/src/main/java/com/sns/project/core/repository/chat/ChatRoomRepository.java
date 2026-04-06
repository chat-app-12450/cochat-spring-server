package com.sns.project.core.repository.chat;

import com.sns.project.core.domain.chat.ChatParticipant;
import com.sns.project.core.domain.chat.ChatRoom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  Optional<ChatRoom> findFirstByNameAndOpenChatTrueOrderByIdAsc(String name);

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
        c.latestMessageAt DESC NULLS LAST,
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
        c.latestMessageAt DESC NULLS LAST,
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
        c.latestMessageAt DESC NULLS LAST,
        c.id DESC
      """)
  List<ChatRoom> findJoinedOpenChatRoomsByUserId(@Param("userId") Long userId);

  @Query("""
      SELECT DISTINCT c
      FROM ChatRoom c
      LEFT JOIN FETCH c.participants p
      LEFT JOIN FETCH p.user
      LEFT JOIN FETCH c.product product
      WHERE c.id IN :roomIds
      """)
  List<ChatRoom> findAllWithParticipantsByIdIn(@Param("roomIds") List<Long> roomIds);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = """
      UPDATE chat_room
      SET location = CASE
        WHEN :latitude IS NULL OR :longitude IS NULL THEN NULL
        ELSE CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)
      END
      WHERE id = :roomId
      """, nativeQuery = true)
  void updateLocation(
      @Param("roomId") Long roomId,
      @Param("latitude") Double latitude,
      @Param("longitude") Double longitude);

  // nearby open chat 검색용 native query
  // 1) 사용자 현재 위치를 1행짜리 CTE(user_location)로 만든다.
  // 2) chat_room.location geography 컬럼과 사용자 위치를 직접 비교한다.
  // 3) ST_DWithin 으로 반경 안의 방만 남긴다.
  // 4) ST_Distance 로 실제 거리를 계산해 가까운 순으로 정렬한다.
  @Query(value = """
      -- 사용자 현재 위치를 geography point 로 만든다.
      WITH user_location AS (
        SELECT CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography) AS point
      )
      SELECT
        cr.id AS roomId,
        -- 사용자 위치와 각 방 사이의 실제 거리를 meters 로 계산한다.
        ST_Distance(cr.location, ul.point) AS distanceMeters
      FROM chat_room cr
      -- user_location 은 1행짜리라서, 모든 방과 "사용자 위치 1개"를 비교하기 위해 CROSS JOIN 한다.
      CROSS JOIN user_location ul
      WHERE cr.open_chat = true
        AND cr.chat_room_type = 'GROUP'
        AND cr.location IS NOT NULL
        AND (
          COALESCE(:keyword, '') = ''
          OR LOWER(cr.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        -- 채팅방 위치와 사용자 위치가 radiusMeters 반경 안에 있는 방만 남긴다.
        AND ST_DWithin(cr.location, ul.point, :radiusMeters)
      -- 가까운 방부터 보여주고, 동일 거리면 id 역순으로 고정 정렬한다.
      ORDER BY distanceMeters ASC, cr.id DESC
      LIMIT :limit
      """, nativeQuery = true)
  List<NearbyOpenChatRoomProjection> findNearbyOpenChatRooms(
      @Param("keyword") String keyword,
      @Param("latitude") double latitude,
      @Param("longitude") double longitude,
      @Param("radiusMeters") double radiusMeters,
      @Param("limit") int limit);

  // @Query("SELECT c FROM ChatRoom c WHERE c.id = :roomId")
  // Optional<ChatRoom> findById(@Param("roomId") Long roomId);
}
