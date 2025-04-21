package com.sns.project.chat_consumer.kafka.consumer;
import com.sns.project.chat_consumer.kafka.dto.request.KafkaDeliverMessage;
import com.sns.project.chat_consumer.service.ChatService;
import java.io.IOException;
import java.util.Set;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageDeliverConsumer {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;

    @KafkaListener(
        topics = "message.deliver",
        groupId = "chat-deliver-group",
        containerFactory = "kafkaListenerContainerFactory")
    public void consume(String json, Acknowledgment ack) throws JsonMappingException, JsonProcessingException {
        KafkaDeliverMessage message = objectMapper.readValue(json, KafkaDeliverMessage.class);
        log.info("📥 Kafka 수신 메시지 (배달): {}", message);

        // persist read status
        Set<Long> readUsers = message.getReadUsers();
        readUsers.forEach(userId -> {
            chatService.saveOrUpdateReadStatus(userId, message.getRoomId(), message.getMessageId());
        });

        log.info("redis pub/sub로 메시지 전파 예정");

//
//        // broadcast
//        try {
//            webSocketHandler.broadcastToRoom(MessageBroadcast.builder()
//                .roomId(message.getRoomId())
//                .senderId(message.getSenderId())
//                .content(message.getContent())
//                .timestamp(message.getReceivedAt())
//                .unreadCount(message.getUnreadCount())
//                .messageId(message.getMessageId())
//                .build());
//        } catch (IOException e) {
//            log.error("🚨 웹소켓 브로드캐스트 실패", e);
//        }

        ack.acknowledge();
    }
}