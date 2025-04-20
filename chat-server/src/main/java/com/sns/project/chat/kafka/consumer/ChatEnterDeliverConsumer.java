package com.sns.project.chat.kafka.consumer;

import com.sns.project.chat.service.ChatService;
import com.sns.project.config.constants.RedisKeys;

import com.sns.project.config.constants.RedisKeys.Chat;
import java.io.IOException;
import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.chat.dto.websocket.ReadBroadcast;
import com.sns.project.chat.handler.ChatWebSocketHandler;
import com.sns.project.chat.kafka.dto.request.KafkaChatEnterDeliverRequest;
import com.sns.project.chat.service.ChatRedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatEnterDeliverConsumer {

    private final ObjectMapper objectMapper;
    private final ChatRedisService chatRedisService;
    private final ChatService chatService;
    private final ChatWebSocketHandler chatWebSocketHandler;

    @KafkaListener(topics = "chat-enter-deliver", groupId = "chat-enter-deliver-group")
    public void consume(String json, Acknowledgment ack) throws JsonProcessingException {
        KafkaChatEnterDeliverRequest request = objectMapper.readValue(json, KafkaChatEnterDeliverRequest.class);
        Long userId = request.getUserId();
        Long prevLastReadId = request.getPrevLastReadId();
        Long newLastReadId = request.getNewLastReadId();
        Long roomId = request.getRoomId();
        chatService.saveOrUpdateReadStatus(userId, roomId, newLastReadId);

        String key = Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);
        Long startRank = chatRedisService.getRank(key, prevLastReadId.toString()).orElse(-1L) ;
        Long endRank = chatRedisService.getRank(key, newLastReadId.toString()).orElse(-1L);
        if (endRank - startRank <= 0) {
            ack.acknowledge();
            return;
        }

        List<String> midMessageIds = chatRedisService.getMidRanksFromZSet(
            RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId), startRank + 1L , endRank
            );
        log.info("🍉 midMessageIds: {}", midMessageIds);
        
        // 메시지 배달
        midMessageIds.forEach(messageId -> {
            ReadBroadcast readBroadcast = ReadBroadcast.builder()
                .roomId(roomId)
                .messageId(Long.parseLong(messageId))
                .unreadCount(chatService.getUnreadCount(roomId, Long.parseLong(messageId)))
                .build();
            System.out.println(readBroadcast);
            try {
                chatWebSocketHandler.broadcastToRoom(readBroadcast);
            } catch (IOException e) {
                log.error("🍉 error broadcasting to room", e);
            }
        });
        
        ack.acknowledge();
    }
}
