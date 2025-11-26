package com.sns.project.chat.websocket;

import com.sns.project.chat.websocket.dto.MessageBroadcast;
import com.sns.project.chat.websocket.dto.RoomScopedPayload;
import com.sns.project.core.kafka.dto.request.KafkaChatEnterRequest;
import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.kafka.producer.ChatEnterProducer;
import com.sns.project.kafka.producer.MsgVectorProducer;
import com.sns.project.chat.service.ChatService;
import com.sns.project.kafka.producer.MsgSaveProducer;

@RequiredArgsConstructor
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final RoomSessionManager roomSessionManager;
    private final ObjectMapper objectMapper;
    private final ChatEnterProducer chatEnterProducer;
    private final MsgVectorProducer msgVectorProducer;
    private final MsgSaveProducer messageBroadcastProducer;
    private final ChatService chatService;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("✅ WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long roomId = (Long) session.getAttributes().get("roomId");
        if (roomId != null) {
            roomSessionManager.removeSession(roomId, session);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject json = new JSONObject(message.getPayload());
        String type = json.getString("type");

        if ("JOIN".equals(type)) {
            Long roomId = json.getLong("roomId");
            Long userId = (Long) session.getAttributes().get("userId");

            // 1) 세션 추가
            session.getAttributes().put("roomId", roomId);
            roomSessionManager.addSession(roomId, session);

            // 2) Kafka로 입장이벤트 발행
            chatEnterProducer.send(
                KafkaChatEnterRequest.builder()
                    .roomId(roomId)
                    .userId(userId)
                    .build()
            );

            return;
        }

        if ("MESSAGE".equals(type)) {
            Long roomId = json.getLong("roomId");
            String msg = json.getString("message");
            Long senderId = (Long) session.getAttributes().get("userId");

            // 1) 여기서 바로 브로드캐스트
            Long time = System.currentTimeMillis();
            broadcastToRoom(
                MessageBroadcast.builder()
                    .roomId(roomId)
                    .senderId(senderId)
                    .content(msg)
                    .receivedAt(time)
                    .build()
            );

            // 2) Kafka로 비동기 이벤트 발행 (DB 저장 + 벡터 생성)
            KafkaNewMsgRequest event = KafkaNewMsgRequest.builder()
                .roomId(roomId)
                .senderId(senderId)
                .content(msg)
                .receivedAt(time)
                .build();

            msgVectorProducer.send(event); // 벡터 생성
            messageBroadcastProducer.sendDeliver(event); // DB 저장, 로그 처리
        }
    }

    public void broadcastToRoom(RoomScopedPayload payload) throws IOException {
        Long roomId = payload.getRoomId();
        Set<WebSocketSession> sessions = roomSessionManager.getSessions(roomId);

        if (sessions == null || sessions.isEmpty()) return;

        String jsonMessage = objectMapper.writeValueAsString(payload);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }
}
