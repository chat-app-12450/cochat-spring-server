package com.sns.project.chat.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import com.sns.project.chat.service.dto.LastReadIdInfo;
import com.sns.project.chat.service.dto.UnreadCountAndReadUsers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
public class RedisLuaService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisLuaService(@Qualifier("chatRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    private static final DefaultRedisScript<List> PROCESS_UNREAD_SCRIPT;
    private static final DefaultRedisScript<List> PROCESS_NEW_MESSAGE_SCRIPT;

    static {
        try {
            // Lua 스크립트 파일 로드
            PROCESS_UNREAD_SCRIPT = new DefaultRedisScript<>(loadScript("lua/process_unread.lua"), List.class);
            PROCESS_NEW_MESSAGE_SCRIPT = new DefaultRedisScript<>(loadScript("lua/process_new_message.lua"), List.class);

        } catch (IOException e) {
            throw new RuntimeException("Lua 스크립트 로드 실패", e);
        }
    }

    // Lua 스크립트 파일 읽기
    private static String loadScript(String path) throws IOException {
        try (var inputStream = new ClassPathResource(path).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }


    // 📌 읽음 처리 메서드 (유저가 메시지를 읽었을 때 호출)
    /*
     * last read id and messages should be cached in redis
     */
    public LastReadIdInfo processUnreadMessages(String lastReadKey, String messageZSetKey, String unreadCountHashKey) {
        List<Object> result = redisTemplate.execute(
            PROCESS_UNREAD_SCRIPT,
            List.of(lastReadKey, messageZSetKey, unreadCountHashKey)
        );

        List<Long> resultList = result.stream()
            .map(Object::toString)
            .map(Long::parseLong)
            .collect(Collectors.toList());
        return LastReadIdInfo.builder()
            .prevLastReadId(resultList.get(0))
            .newLastReadId(resultList.get(1))
            .build();
    }

    // 📌 새 메시지 처리 메서드 (새로운 메시지가 왔을 때 호출)
    /*
     * participantsKey: 채팅방 참여자 목록
     * connectedUsersKey: 채팅방 참여자 중 온라인 상태인 사용자 목록
     * unreadCountHashKey: 채팅방 참여자 중 읽지 않은 메시지 수
     * lastReadKeyPattern: 채팅방 참여자 중 마지막으로 읽은 메시지 ID
     * messageZSetKey: 채팅방 메시지 목록
     * roomId: 채팅방 ID
     * messageId: 메시지 ID
     * senderId: 메시지 보낸 사용자 ID
     * 
     * praticipants, lastread id, messages should be cached in redis
     */
    public UnreadCountAndReadUsers processNewMessage(
    String participantsKey,
    String connectedUsersKey,
    String unreadCountHashKey,
    String lastReadKeyPattern,
    String messageZSetKey,
    String roomId,
    String messageId,
    String senderId
) {
    Object rawResult = redisTemplate.execute(
        PROCESS_NEW_MESSAGE_SCRIPT,
    List.of(participantsKey, connectedUsersKey, unreadCountHashKey, lastReadKeyPattern, messageZSetKey),
        roomId, messageId, senderId
    );


    if (rawResult instanceof List resultList && resultList.size() == 2) {
        int unreadCount = Integer.parseInt(resultList.get(0).toString());



        List<Object> rawReadUserIds = (List<Object>) resultList.get(1);
        Set<Long> readUserIds = rawReadUserIds.stream()
            .map(Object::toString)
            .map(Long::parseLong)
            .collect(Collectors.toSet());

        return UnreadCountAndReadUsers.builder()
            .unreadCount(unreadCount)
            .readUsers(readUserIds)
            .build();
    }

    return UnreadCountAndReadUsers.builder()
        .unreadCount(0)
        .readUsers(Set.of())
        .build();
}
}
