package com.sns.project.chat_consumer.kafka.processor;

import com.sns.project.chat_consumer.service.ChatRedisService;
import com.sns.project.chat_consumer.service.ChatService;
import com.sns.project.core.constants.RedisKeys;
import com.sns.project.core.kafka.dto.request.KafkaMsgBroadcastRequest;
import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageProcessor {

    private final ChatService chatService;
    private final ChatRedisService chatRedisService;

    public KafkaMsgBroadcastRequest process(KafkaNewMsgRequest message) {
        Long roomId = message.getRoomId();
        String clientMessageId = message.getClientMessageId();
        Long receivedAt = message.getReceivedAt();
        String content = message.getContent();
        Long senderId = message.getSenderId();
        log.info("📥 Kafka 수신 메시지: roomId={}, messageId={}", roomId, clientMessageId);

        Long messageId = chatService.saveMessage(roomId, senderId, content, clientMessageId);

        String messageZSetKey = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);
        chatRedisService.addToZSet(messageZSetKey, messageId.toString(), messageId);

        KafkaMsgBroadcastRequest deliverMessage = KafkaMsgBroadcastRequest.builder()
            .messageId(messageId)
            .roomId(roomId)
            .senderId(senderId)
            .content(content)
            .receivedAt(receivedAt)
            .build();
        return deliverMessage;
    }

}
