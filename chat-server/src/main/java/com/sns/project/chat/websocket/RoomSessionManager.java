package com.sns.project.chat.websocket;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import lombok.extern.slf4j.Slf4j;

/**
 * 채팅방(roomId) 별 WebSocket 세션을 관리하는 매니저
 */
@Slf4j
@Component
public class RoomSessionManager {

    // roomId → 세션 Set 매핑
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    /**
     * roomId에 WebSocket 세션 추가
     */
    public void addSession(Long roomId, WebSocketSession session) {
        roomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
        roomSessions.get(roomId).add(session);
        log.info("✅ 세션 추가: roomId={}, sessionId={}", roomId, session.getId());
    }

    /**
     * roomId에서 WebSocket 세션 제거
     */
    public void removeSession(Long roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            log.info("👋 세션 제거: roomId={}, sessionId={}", roomId, session.getId());

            // 방 안에 세션이 하나도 없으면 방 삭제할 수도 있음 (선택)
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
                log.info("🧹 방 세션 전부 종료됨, roomId={} 제거", roomId);
            }
        }
    }

    /**
     * 특정 roomId에 연결된 모든 세션 반환
     */
    public Set<WebSocketSession> getSessions(Long roomId) {
        return roomSessions.getOrDefault(roomId, Collections.emptySet());
    }
}
