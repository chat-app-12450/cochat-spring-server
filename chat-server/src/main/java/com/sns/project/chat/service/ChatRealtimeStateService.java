package com.sns.project.chat.service;

import com.sns.project.core.repository.chat.ChatParticipantRepository;
import com.sns.project.core.repository.chat.ChatMessageRepository;
import com.sns.project.core.repository.chat.RoomUnreadCountProjection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRealtimeStateService {

    private static final Duration PRESENCE_TTL = Duration.ofHours(6);

    private final ChatRedisService chatRedisService;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;

    public void enterRoom(Long roomId, Long userId) {
        String key = presenceKey(roomId, userId);
        chatRedisService.incrementValue(key, 1);
        chatRedisService.expire(key, PRESENCE_TTL);
    }

    public void leaveRoom(Long roomId, Long userId) {
        String key = presenceKey(roomId, userId);
        Long current = chatRedisService.getLongValue(key).orElse(0L);
        if (current <= 1) {
            chatRedisService.delete(key);
            return;
        }
        chatRedisService.incrementValue(key, -1);
        chatRedisService.expire(key, PRESENCE_TTL);
    }

    public boolean isUserActiveInRoom(Long roomId, Long userId) {
        return chatRedisService.getLongValue(presenceKey(roomId, userId))
            .map(count -> count > 0)
            .orElse(false);
    }

    // 채팅방 참가자들의 방안읽음수 갱신
    public void incrementUnreadCounts(Long roomId, Long senderId) {
        List<Long> participantIds = chatParticipantRepository.findActiveParticipantIdsByRoomId(roomId);
        for (Long participantId : participantIds) {
            if (participantId.equals(senderId)) {
                continue;
            }
            if (isUserActiveInRoom(roomId, participantId)) {
                continue;
            }
            chatRedisService.incrementHash(unreadCountKey(participantId), String.valueOf(roomId), 1);
        }
    }

    // 메시지별 unreadCount 초기값은 현재 active 멤버 전체를 기준으로 잡고,
    // 이후 READ 이벤트로만 감소시킨다.
    public long countInitialMessageUnreadUsers(Long roomId, Long senderId) {
        long unreadCount = 0;
        List<Long> participantIds = chatParticipantRepository.findActiveParticipantIdsByRoomId(roomId);
        for (Long participantId : participantIds) {
            if (participantId.equals(senderId)) {
                continue;
            }
            unreadCount += 1;
        }
        return unreadCount;
    }

    public void clearUnreadCount(Long roomId, Long userId) {
        // 0을 명시적으로 넣어두면 다음 방 목록 조회 때 "없는 field"와 구분할 수 있다.
        chatRedisService.setHashValue(unreadCountKey(userId), String.valueOf(roomId), "0");
    }

    public Map<Long, Long> getUnreadCounts(Long userId, List<Long> roomIds) {
        Map<Long, Long> unreadCountByRoomId = new HashMap<>();
        List<Long> missingRoomIds = new ArrayList<>();
        String key = unreadCountKey(userId);

        for (Long roomId : roomIds) {
            String roomIdKey = String.valueOf(roomId);
            // key: chat:unread:user:${userId}
            // hashkey: room id
            // hashvalue: unread count
            chatRedisService.getHashValue(key, roomIdKey)
                .ifPresentOrElse(value -> {
                    Long count = Long.parseLong(value);
                    if (count > 0) {
                        unreadCountByRoomId.put(roomId, count);
                    }
                }, () -> missingRoomIds.add(roomId));
        }

        if (missingRoomIds.isEmpty()) {
            return unreadCountByRoomId;
        }

        Map<Long, Long> rebuiltUnreadCounts = rebuildUnreadCounts(userId, missingRoomIds);
        for (Long roomId : missingRoomIds) {
            Long unreadCount = rebuiltUnreadCounts.getOrDefault(roomId, 0L);
            chatRedisService.setHashValue(key, String.valueOf(roomId), String.valueOf(unreadCount));
            if (unreadCount > 0) {
                unreadCountByRoomId.put(roomId, unreadCount);
            }
        }

        return unreadCountByRoomId;
    }

    private Map<Long, Long> rebuildUnreadCounts(Long userId, List<Long> roomIds) {
        Map<Long, Long> unreadCountByRoomId = new HashMap<>();
        if (roomIds.isEmpty()) {
            return unreadCountByRoomId;
        }

        // Redis가 비었을 때만 DB 원본 포인터(lastReadSeq) 기준으로 unread를 재계산한다.
        // 평소 요청마다 COUNT 쿼리를 치지 않고, 캐시 miss 복구 경로로만 쓰는 것이 목적이다.
        List<RoomUnreadCountProjection> unreadCounts = chatMessageRepository.countUnreadMessagesByRoomIds(userId, roomIds);
        for (RoomUnreadCountProjection unreadCount : unreadCounts) {
            unreadCountByRoomId.put(unreadCount.getRoomId(), unreadCount.getUnreadCount());
        }
        return unreadCountByRoomId;
    }

    private String unreadCountKey(Long userId) {
        return "chat:unread:user:" + userId;
    }

    private String presenceKey(Long roomId, Long userId) {
        return "chat:presence:room:" + roomId + ":user:" + userId;
    }
}
