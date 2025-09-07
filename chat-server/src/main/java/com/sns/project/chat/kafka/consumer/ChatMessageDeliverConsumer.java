package com.sns.project.chat.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.chat.websocket.RoomSessionManager;
import com.sns.project.chat.websocket.dto.MessageBroadcast;
import com.sns.project.chat.websocket.dto.RoomScopedPayload;
import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageDeliverConsumer {

    private final ObjectMapper objectMapper;
    private final RoomSessionManager roomSessionManager; // ✨ 세션 매니저 주입받기

    // message.broadcast
    @KafkaListener(
        topics = "message.broadcast"
        // groupId = "chat-broadcast-server1" // 서버마다 다르게 (chat-broadcast-server2, 3, ...)
        // containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(KafkaNewMsgRequest broadcastMessage, Acknowledgment ack) throws JsonProcessingException {
        // KafkaMsgBroadcastRequest broadcastMessage = objectMapper.readValue(json, KafkaMsgBroadcastRequest.class);

        log.info("📥 broadcast 수신: roomId={}, senderId={}, content={}", 
                 broadcastMessage.getRoomId(), broadcastMessage.getSenderId(), broadcastMessage.getContent());

        // WebSocket으로 Push
        sendToRoom(broadcastMessage);

        ack.acknowledge();
    }

    private void sendToRoom(KafkaNewMsgRequest broadcastMessage) {
        Long roomId = broadcastMessage.getRoomId();
        Set<WebSocketSession> sessions = roomSessionManager.getSessions(roomId);

        if (sessions == null || sessions.isEmpty()) {
            log.info("🚫 roomId={} 세션 없음. 메시지 버림", roomId);
            return;
        }

        RoomScopedPayload payload = MessageBroadcast.builder()
                .roomId(roomId)
                .senderId(broadcastMessage.getSenderId())
                .content(broadcastMessage.getContent())
                .receivedAt(broadcastMessage.getReceivedAt())
                .build();

        try {
            String jsonMessage = objectMapper.writeValueAsString(payload);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonMessage));
                }
            }

            log.info("📩 roomId={} 에 {}개 세션에 메시지 전송 완료", roomId, sessions.size());

        } catch (IOException e) {
            log.error("❌ WebSocket 전송 실패: roomId={}, 에러={}", roomId, e.getMessage());
        }
    }
}
