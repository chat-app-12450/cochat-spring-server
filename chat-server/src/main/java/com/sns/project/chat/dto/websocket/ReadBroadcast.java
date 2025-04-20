package com.sns.project.chat.dto.websocket;

import com.sns.project.chat.dto.websocket.RoomScopedPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadBroadcast implements RoomScopedPayload {
    private final String type = "READ";
    private Long roomId;
    private Long messageId;
    private int unreadCount;

    @Override
    public Long getRoomId() {
        return roomId;
    }
}
