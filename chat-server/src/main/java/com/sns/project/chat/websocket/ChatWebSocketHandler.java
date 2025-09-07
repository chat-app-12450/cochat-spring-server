package com.sns.project.chat.websocket;

import com.sns.project.chat.websocket.dto.RoomScopedPayload;
import com.sns.project.core.kafka.dto.request.KafkaChatEnterRequest;
import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import java.io.IOException;
import java.time.ZoneOffset;
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
import com.sns.project.chat.kafka.producer.ChatEnterProducer;
import com.sns.project.chat.kafka.producer.MessageVectorProducer;
import com.sns.project.chat.service.ChatService;
import com.sns.project.chat.kafka.producer.MessageBroadcastProducer;
import com.sns.project.core.domain.chat.ChatMessage;

@RequiredArgsConstructor
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final RoomSessionManager roomSessionManager;
    private final ObjectMapper objectMapper;
    private final ChatEnterProducer chatEnterProducer;
    private final MessageVectorProducer messageVectorProducer;
    private final MessageBroadcastProducer messageBroadcastProducer;
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
            session.getAttributes().put("roomId", roomId);
            roomSessionManager.addSession(roomId, session);

            chatEnterProducer.send(KafkaChatEnterRequest.builder()
                .roomId(roomId)
                .userId(userId)
                .build());

        } else if ("MESSAGE".equals(type)) {
            Long roomId = json.getLong("roomId");
            String msg = json.getString("message");
            Long senderId = (Long) session.getAttributes().get("userId");

            ChatMessage savedMessage = chatService.saveMessage(roomId, senderId, msg);
            KafkaNewMsgRequest kafkaNewMsgRequest = KafkaNewMsgRequest.builder()
                .roomId(roomId)
                .senderId(senderId)
                .content(msg)
                .receivedAt(savedMessage.getReceivedAt().toEpochSecond(ZoneOffset.UTC))
                .messageId(savedMessage.getId())
                .build();
                
            log.info("💙 새로운 메시지 도착 {} {}", kafkaNewMsgRequest.getRoomId(), kafkaNewMsgRequest.getSenderId());
            messageVectorProducer.send(kafkaNewMsgRequest);
            messageBroadcastProducer.sendDeliver(kafkaNewMsgRequest);
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
