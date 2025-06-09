package com.sns.project.chat.websocket.dto;

public interface RoomScopedPayload extends WebsocketPayload {
    Long getRoomId(); // roomId가 반드시 있는 메시지에만 사용
}
