package com.sns.project.chat.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.chat.websocket.dto.MessageBroadcast;
import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageCreatedBroadcastConsumer {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // Outbox relay가 발행한 chat.message.created 원본 이벤트를 받는다.
    // 브로드캐스트는 별도 역할이므로 groupId를 chat-broadcast로 분리했다.
    // Outbox 토픽 payload는 문자열(JSON)이므로 String 기반 리스너 팩토리를 사용한다.
    @KafkaListener(
        topics = "${app.kafka.topics.chat-message-created}",
        groupId = "chat-broadcast",
        containerFactory = "outboxStringKafkaListenerContainerFactory"
    )
    public void consume(String payload, Acknowledgment ack) throws Exception {
        KafkaNewMsgRequest broadcastMessage = objectMapper.readValue(payload, KafkaNewMsgRequest.class);
        log.info("broadcast received from outbox: roomId={}, senderId={}",
            broadcastMessage.getRoomId(), broadcastMessage.getSenderId());

        sendToRoom(broadcastMessage);
        ack.acknowledge();
    }

    private void sendToRoom(KafkaNewMsgRequest broadcastMessage) {
        Long roomId = broadcastMessage.getRoomId();
        MessageBroadcast payload = MessageBroadcast.builder()
            .messageId(broadcastMessage.getMessageId())
            .messageSeq(broadcastMessage.getMessageSeq())
            .clientMessageId(broadcastMessage.getClientMessageId())
            .roomId(roomId)
            .senderId(broadcastMessage.getSenderId())
            .content(broadcastMessage.getContent())
            .receivedAt(broadcastMessage.getReceivedAt())
            .unreadCount(broadcastMessage.getUnreadCount())
            .build();

        messagingTemplate.convertAndSend("/topic/chat/rooms/" + roomId, payload);
        log.info("stomp broadcast delivered: roomId={}", roomId);
    }
}
