package com.sns.project.chat.service;

import com.sns.project.chat.constants.RedisKeys;
import com.sns.project.chat.service.dto.LastReadIdInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRedisService chatRedisService;

    /**
     * 유저의 읽음 처리 상태를 저장합니다. (실제 구현은 생략)
     */
    public void saveOrUpdateReadStatus(Long userId, Long roomId, Long messageId) {
        log.info("읽음 상태 저장: userId={}, roomId={}, messageId={}", userId, roomId, messageId);
        // 실제 저장 로직은 생략 (DB 의존성 제거)
    }

    /**
     * 모든 메시지를 읽음으로 처리합니다. (간소화된 구현)
     */
    public LastReadIdInfo readAllMessages(Long userId, Long roomId) {
        log.info("모든 메시지 읽음 처리: userId={}, roomId={}", userId, roomId);
        return LastReadIdInfo.builder()
            .prevLastReadId(0L)
            .newLastReadId(100L) // 예시 값
            .build();
    }

    /**
     * 메시지를 저장합니다. (간소화된 구현)
     */
    public Long saveMessage(Long roomId, Long senderId, String message, String clientMessageId) {
        log.info("메시지 저장: roomId={}, senderId={}, message={}, clientMessageId={}", 
                 roomId, senderId, message, clientMessageId);
        return 123L; // 예시 메시지 ID
    }

    /**
     * 메시지별 안읽은 수를 조회합니다. (간소화된 구현)
     */
    public int getUnreadCount(Long roomId, Long messageId) {
        log.info("안읽은 수 조회: roomId={}, messageId={}", roomId, messageId);
        String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();
        return chatRedisService.getHashValue(unreadCountKey, messageId.toString())
            .map(Integer::parseInt)
            .orElse(calculateUnreadCount(roomId, messageId));
    }

    /**
     * 안읽은 수를 계산합니다. (간소화된 구현)
     */
    public int calculateUnreadCount(Long roomId, Long messageId) {
        log.info("안읽은 수 계산: roomId={}, messageId={}", roomId, messageId);
        return 2; // 예시 값
    }
} 