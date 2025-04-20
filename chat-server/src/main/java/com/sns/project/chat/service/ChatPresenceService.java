package com.sns.project.chat.service;

import com.sns.project.config.constants.RedisKeys.Chat;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatPresenceService {
    private final ChatRedisService stringRedisService;

    // 사용자가 채팅방에 들어오면 Redis에 저장
    public void userEnteredRoom(Long roomId, Long userId) {
        String key = Chat.CONNECTED_USERS_SET_KEY.getConnectedKey(roomId);
        stringRedisService.setValueWithExpirationInSet(key, userId.toString(), 10000 *60 * 60);
    }

    // 사용자가 채팅방을 나가면 Redis에서 제거
    public void userLeftRoom(Long roomId, Long userId) {
        String key = Chat.CONNECTED_USERS_SET_KEY.getConnectedKey(roomId);
        stringRedisService.removeFromSet(key, userId.toString());
    }

    // 사용자가 현재 채팅방에 있는지 확인
    public boolean isUserInRoom(Long roomId, Long userId) {
        String key = Chat.CONNECTED_USERS_SET_KEY.getConnectedKey(roomId);
        return stringRedisService.isSetMember(key, userId.toString());
    }
}
