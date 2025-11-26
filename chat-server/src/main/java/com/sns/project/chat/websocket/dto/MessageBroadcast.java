package com.sns.project.chat.websocket.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageBroadcast implements RoomScopedPayload {
    private final String type = "MESSAGE";
    private Long roomId;
    private Long senderId;
    private String content;
    private Long receivedAt;

    @Override
    public Long getRoomId() {
        return roomId;
    }

//    public MessageBroadcast(Long roomId, Long senderId, String content, Long receivedAt){
//        this.roomId = roomId;
//        this.senderId = senderId;
//        this.content = content;
//        this.receivedAt = receivedAt;
//    }
}
