package com.sns.project.chat.dto.websocket;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoinBroadcast implements RoomScopedPayload {
    private final String type = "JOIN";
    private Long roomId;
    private Long senderId;

    @Override
    public Long getRoomId() {
        return roomId;
    }
}

