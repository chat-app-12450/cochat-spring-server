package com.sns.project.chat.service;


import com.sns.project.chat.constants.RedisKeys.Chat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.sns.project.chat.constants.RedisKeys;



/**
 * 채팅방 사용자 입장/퇴장 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatPresenceService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 사용자가 채팅방에 입장
     */
    public void enterChatRoom(String userId, String roomId) {
        String key = Chat.CONNECTED_USERS_SET_KEY.getConnectedKey(roomId);
        redisTemplate.opsForSet().add(key, userId);
        log.info("User {} entered chat room {}", userId, roomId);
    }

    /**
     * 사용자가 채팅방에서 퇴장
     */
    public void exitChatRoom(String userId, String roomId) {
        String key = Chat.CONNECTED_USERS_SET_KEY.getConnectedKey(roomId);
        redisTemplate.opsForSet().remove(key, userId);
        log.info("User {} exited chat room {}", userId, roomId);
    }

    /**
     * 사용자가 채팅방에 있는지 확인
     */
    public boolean isUserInChatRoom(String userId, String roomId) {
        String key = Chat.CONNECTED_USERS_SET_KEY.getConnectedKey(roomId);
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId);
        return isMember != null && isMember;
    }
} 