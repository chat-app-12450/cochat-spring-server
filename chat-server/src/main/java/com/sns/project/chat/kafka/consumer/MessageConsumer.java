package com.sns.project.chat.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.chat.kafka.dto.request.KafkaDeliverMessage;
import com.sns.project.chat.kafka.dto.request.KafkaNewMsgRequest;
import com.sns.project.chat.kafka.producer.MessageDeliverProducer;
import com.sns.project.chat.service.ChatRedisService;
import com.sns.project.chat.service.ChatService;
import com.sns.project.chat.service.UnreadCountService;
import com.sns.project.chat.service.dto.UnreadCountAndReadUsers;
import com.sns.project.config.constants.RedisKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumer {

    private final ChatRedisService chatRedisService;
    private final ObjectMapper objectMapper;
    private final UnreadCountService unreadCountService;
    private final ChatService chatService;
    private final MessageDeliverProducer messageDeliverProducer;    

    @KafkaListener(topics = "message.received", groupId = "chat-received-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(String json, Acknowledgment ack) throws JsonProcessingException {
        
        KafkaNewMsgRequest message = objectMapper.readValue(json, KafkaNewMsgRequest.class);
        Long roomId = message.getRoomId();
        String clientMessageId = message.getClientMessageId();
        Long receivedAt = message.getReceivedAt();
        String content = message.getContent();
        Long senderId = message.getSenderId();
        log.info("📥 Kafka 수신 메시지: roomId={}, messageId={}", roomId, clientMessageId);

        // 1. 메시지 저장 (DB)
        Long messageId = chatService.saveMessage(roomId, senderId, content, clientMessageId);

        // 2. 메시지 캐시 (Redis)
        String messageZSetKey = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);
        chatRedisService.addToZSet(messageZSetKey, messageId.toString(), receivedAt);

        // 3. unread 계산
        UnreadCountAndReadUsers result = unreadCountService.handleUnreadCalculation(
            message.getRoomId(),
            messageId,
            message.getSenderId()
        );

        // 4. broadcast & persist
        KafkaDeliverMessage deliverMessage = KafkaDeliverMessage.builder()
        .messageId(messageId)
        .roomId(roomId)
        .senderId(senderId)
        .content(content)
        .receivedAt(receivedAt)
        .unreadCount(result.getUnreadCount())
        .readUsers(result.getReadUsers())
        .build();
       messageDeliverProducer.sendDeliver(deliverMessage);

        ack.acknowledge();
    }
}
