package com.sns.project.chat.service;

import com.sns.project.core.repository.chat.ChatParticipantRepository;
import java.time.Duration;
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

    public void incrementUnreadCounts(Long roomId, Long senderId) {
        List<Long> participantIds = chatParticipantRepository.findParticipantIdsByRoomId(roomId);
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

    public void clearUnreadCount(Long roomId, Long userId) {
        chatRedisService.deleteHashValue(unreadCountKey(userId), String.valueOf(roomId));
    }

    public Map<Long, Long> getUnreadCounts(Long userId, List<Long> roomIds) {
        Map<Long, Long> unreadCountByRoomId = new HashMap<>();
        String key = unreadCountKey(userId);
        for (Long roomId : roomIds) {
            chatRedisService.getHashValue(key, String.valueOf(roomId))
                .map(Long::parseLong)
                .filter(count -> count > 0)
                .ifPresent(count -> unreadCountByRoomId.put(roomId, count));
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
