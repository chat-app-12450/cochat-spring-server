package com.sns.project.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.chat.kafka.dto.request.KafkaChatEnterRequest;
import com.sns.project.chat.kafka.dto.request.KafkaNewMsgRequest;
import com.sns.project.chat.dto.websocket.RoomScopedPayload;
import com.sns.project.chat.kafka.producer.ChatEnterProducer;
import com.sns.project.chat.kafka.producer.MessageProducer;
import com.sns.project.chat.service.ChatPresenceService;
import com.sns.project.chat.service.ChatRedisService;
import com.sns.project.chat.service.ChatService;
import com.sns.project.config.constants.RedisKeys.Chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ChatPresenceService chatPresenceService;
    private final MessageProducer messageProducer; // ✅ Kafka 프로듀서
    private final ChatService chatService;
    private final ChatRedisService chatRedisService;
    private final ChatEnterProducer chatEnterProducer;
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("✅ WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        Long roomId = (Long) session.getAttributes().get("roomId");

        if (userId != null && roomId != null) {
            chatPresenceService.userLeftRoom(roomId, userId);
            log.info("👋 User {} left room {}", userId, roomId);
            roomSessions.getOrDefault(roomId, new HashSet<>()).remove(session);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject json = new JSONObject(message.getPayload());
        String type = json.getString("type");
        log.info("json: {}", json);
        if ("JOIN".equals(type)) {
            Long roomId = json.getLong("roomId");
            Long userId = (Long) session.getAttributes().get("userId");

            session.getAttributes().put("roomId", roomId);
            roomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
            roomSessions.get(roomId).add(session);

            chatEnterProducer.send(KafkaChatEnterRequest.builder()
                .roomId(roomId)
                .userId(userId)
                .build());



        } else if ("MESSAGE".equals(type)) {
            Long roomId = json.getLong("roomId");
            String msg = json.getString("message");
            Long senderId = (Long) session.getAttributes().get("userId");
            String clientMessageId = json.getString("clientMessageId");

            messageProducer.send(KafkaNewMsgRequest.builder()
                .roomId(roomId)
                .senderId(senderId)
                .content(msg)
                .clientMessageId(clientMessageId)
                .receivedAt(System.currentTimeMillis())
                .build());
        }
    }

    public void broadcastToRoom(RoomScopedPayload payload) throws IOException {
        Long roomId = payload.getRoomId();
        log.info("🍉 broadcast to room : {}", roomId);
        log.info("type: {}", payload.getType());
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        log.info("session : {}", sessions);
        if (sessions == null || sessions.isEmpty()) return;
        String jsonMessage = objectMapper.writeValueAsString(payload);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(jsonMessage));
            }
        }
        log.info("📩 Message sent to room {}: {}", roomId, payload);
    }
}
