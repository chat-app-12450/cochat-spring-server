package com.sns.project.chat.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageBroadcast {
    private final String type = "MESSAGE";
    private Long messageId;
    private String clientMessageId;
    private Long roomId;
    private Long senderId;
    private String content;
    private Long receivedAt;
}
