package com.sns.project.chat.kafka.consumer;

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
public class ChatMessageDeliverConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    // message.broadcast
    @KafkaListener(
        topics = "${app.kafka.topics.message-broadcast}"
        // groupId = "chat-broadcast-server1" // 서버마다 다르게 (chat-broadcast-server2, 3, ...)
        // containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(KafkaNewMsgRequest broadcastMessage, Acknowledgment ack) {
        // 메시지 본문은 사용자 payload 이므로 운영 로그에 남기지 않는다.
        log.info("broadcast received: roomId={}, senderId={}",
                 broadcastMessage.getRoomId(), broadcastMessage.getSenderId());

        sendToRoom(broadcastMessage);
        ack.acknowledge();
    }

    private void sendToRoom(KafkaNewMsgRequest broadcastMessage) {
        Long roomId = broadcastMessage.getRoomId();
        MessageBroadcast payload = MessageBroadcast.builder()
            .messageId(broadcastMessage.getMessageId())
            .roomId(roomId)
            .senderId(broadcastMessage.getSenderId())
            .content(broadcastMessage.getContent())
            .receivedAt(broadcastMessage.getReceivedAt())
            .build();

        messagingTemplate.convertAndSend("/topic/chat/rooms/" + roomId, payload);
        log.info("stomp broadcast delivered: roomId={}", roomId);
    }
}
