package com.sns.project.chat.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.core.domain.chat.ChatMessage;
import com.sns.project.core.domain.outbox.OutboxAggregateType;
import com.sns.project.core.domain.outbox.OutboxEvent;
import com.sns.project.core.domain.outbox.OutboxEventType;
import com.sns.project.core.kafka.dto.event.ChatRoomReadKafkaEvent;
import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import com.sns.project.core.repository.outbox.OutboxEventRepository;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatOutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.chat-message-created}")
    private String chatMessageCreatedTopicName;

    @Value("${app.kafka.topics.chat-room-read}")
    private String chatRoomReadTopicName;

    public void enqueueChatMessageCreated(ChatMessage chatMessage) {
        KafkaNewMsgRequest payload = KafkaNewMsgRequest.builder()
            .roomId(chatMessage.getChatRoom().getId())
            .senderId(chatMessage.getSender().getId())
            .content(chatMessage.getMessage())
            .receivedAt(chatMessage.getReceivedAt().toEpochSecond(ZoneOffset.UTC))
            .messageId(chatMessage.getId())
            .build();

        outboxEventRepository.save(OutboxEvent.pending(
            OutboxAggregateType.CHAT_MESSAGE,
            chatMessage.getId(),
            OutboxEventType.CHAT_MESSAGE_CREATED,
            chatMessageCreatedTopicName,
            String.valueOf(chatMessage.getChatRoom().getId()),
            //  payload를 JSON으로 직렬화해서 outbox row 저장
            toJson(payload)
        ));
    }

    public void enqueueChatRoomRead(Long roomId, Long userId, Long messageId) {
        if (messageId == null) {
            return;
        }

        ChatRoomReadKafkaEvent payload = ChatRoomReadKafkaEvent.builder()
            .roomId(roomId)
            .userId(userId)
            .messageId(messageId)
            .build();

        outboxEventRepository.save(OutboxEvent.pending(
            OutboxAggregateType.CHAT_READ_STATUS,
            roomId,
            OutboxEventType.CHAT_ROOM_READ,
            chatRoomReadTopicName,
            String.valueOf(roomId),
            // payload를 JSON으로 직렬화해서 outbox row 저장
            toJson(payload)
        ));
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox payload serialization failed", e);
        }
    }
}
