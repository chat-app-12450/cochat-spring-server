package com.sns.project.chat.util;

import com.sns.project.chat.service.ChatRedisService;
import com.sns.project.config.constants.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheManager {

    private final ChatRedisService chatRedisService;

    /** ✅ 메시지 ZSet 존재 여부 */
    public boolean isMessageZSetReady(Long roomId) {
        String zsetKey = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);
        return hasDataInZSet(zsetKey);
    }

    /** ✅ 참여자 Set 존재 여부 */
    public boolean isParticipantsSetReady(Long roomId) {
        String setKey = RedisKeys.Chat.CHAT_ROOM_PARTICIPANTS_SET_KEY.getParticipants(roomId);
        return hasDataInSet(setKey);
    }


    /** ✅ 유저별 lastReadId 존재 여부 */
    public boolean isLastReadKeyReady(Long userId, Long roomId) {
        String key = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID.getLastReadMessageKey(userId, roomId);
        return chatRedisService.hasKey(key) && chatRedisService.getValue(key) != null;
    }

    /** ✅ ZSet에 데이터 있는지 */
    private boolean hasDataInZSet(String key) {
        if (!Boolean.TRUE.equals(chatRedisService.hasKey(key))) return false;
        Long size = chatRedisService.getZSetSize(key);
        return size != null && size > 0;
    }

    /** ✅ Set에 데이터 있는지 */
    private boolean hasDataInSet(String key) {
        if (!Boolean.TRUE.equals(chatRedisService.hasKey(key))) return false;
        Long size = chatRedisService.getSetSize(key);
        return size != null && size > 0;
    }

}
