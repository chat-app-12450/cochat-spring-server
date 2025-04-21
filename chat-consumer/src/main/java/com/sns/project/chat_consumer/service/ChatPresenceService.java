package com.sns.project.chat_consumer.service;

import com.sns.project.core.constants.RedisKeys.Chat;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatPresenceService {
    private final ChatRedisService chatRedisService;

    // 사용자가 채팅방에 들어오면 Redis에 저장
    public void userEnteredRoom(Long roomId, Long userId) {
        String key = Chat.CONNECTED_USERS_SET_KEY.getConnectedKey(roomId.toString());
        chatRedisService.setValueWithExpirationInSet(key, userId.toString(), 10000 * 60 * 60);
        log.info("User {} entered chat room {}", userId, roomId);
    }

    // 사용자가 채팅방을 나가면 Redis에서 제거
    public void userLeftRoom(Long roomId, Long userId) {
        String key = Chat.CONNECTED_USERS_SET_KEY.getConnectedKey(roomId.toString());
        chatRedisService.removeFromSet(key, userId.toString());
        log.info("User {} exited chat room {}", userId, roomId);
    }

    // 사용자가 현재 채팅방에 있는지 확인
    public boolean isUserInRoom(Long roomId, Long userId) {
        String key = Chat.CONNECTED_USERS_SET_KEY.getConnectedKey(roomId.toString());
        return chatRedisService.isSetMember(key, userId.toString());
    }
} 