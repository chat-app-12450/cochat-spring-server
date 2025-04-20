package com.sns.project.chat.service;

import com.sns.project.chat.util.RedisCacheManager;
import com.sns.project.config.constants.RedisKeys;
import com.sns.project.domain.chat.ChatMessage;
import com.sns.project.domain.chat.ChatReadStatus;
import com.sns.project.repository.chat.ChatMessageRepository;
import com.sns.project.repository.chat.ChatParticipantRepository;
import com.sns.project.repository.chat.ChatReadStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPreloadService {

    private final ChatParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final RedisCacheManager redisCacheManager;
    private final ChatReadStatusRepository chatReadStatusRepository;
    private final ChatRedisService chatRedisService;

    /**
     * ✅ 채팅방 메시지, 참여자, 유저 읽음 정보 캐시 자동 로딩
     */
    public void preloadRoomData(Long roomId) {
        List<Long> participantIds = loadParticipants(roomId);

        if (!redisCacheManager.isMessageZSetReady(roomId)) {
            loadMessages(roomId);
        }

        for (Long userId : participantIds) {
            if (!redisCacheManager.isLastReadKeyReady(userId, roomId)) {
                initLastReadId(userId, roomId);
            }
        }
    }

    /** 참여자 목록을 Redis Set에 저장 */
    private List<Long> loadParticipants(Long roomId) {
        String key = RedisKeys.Chat.CHAT_ROOM_PARTICIPANTS_SET_KEY.getParticipants(roomId);
        List<Long> participantIds = participantRepository.findParticipantIdsByRoomId(roomId);

        if (participantIds == null || participantIds.isEmpty()) {
            log.warn("⚠️ 참여자 없음: roomId={}", roomId);
            return Collections.emptyList();
        }

        participantIds.forEach(id ->
            chatRedisService.addToSet(key, String.valueOf(id))
        );
        log.info("✅ 참여자 Set 캐싱 완료: roomId={}, users={}", roomId, participantIds);
        return participantIds;
    }

    /** 메시지들을 Redis ZSet에 저장 (id 기준 정렬) */
    private void loadMessages(Long roomId) {
        String key = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);
        List<ChatMessage> messages = messageRepository.findAllByRoomId(roomId);

        if (messages == null || messages.isEmpty()) {
            log.warn("⚠️ 메시지 없음: roomId={}", roomId);
            return;
        }

        for (ChatMessage message : messages) {
            chatRedisService.addToZSet(key, message.getId().toString(), message.getId());
        }
        log.info("✅ 메시지 ZSet 캐싱 완료: roomId={}, count={}", roomId, messages.size());
    }

    /** lastReadKey 없으면 초기화 (-1) */
    private void initLastReadId(Long userId, Long roomId) {
        String key = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID.getLastReadMessageKey(userId, roomId);

        Optional<ChatReadStatus> readStatus = chatReadStatusRepository.findByUserIdAndChatRoomId(userId, roomId);
        if (readStatus.isEmpty()) {
            chatRedisService.setValue(key, "-1");
            log.info("✅ lastReadKey 초기화: userId={}, roomId={}", userId, roomId);

        } else {
            chatRedisService.setValue(key, readStatus.get().getLastReadMessageId().toString());
            log.info("✅ lastReadKey 캐싱 완료: userId={}, roomId={}", userId, roomId);
        }
    }
}
