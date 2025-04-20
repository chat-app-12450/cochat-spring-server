package com.sns.project.chat.kafka.dto.request;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class KafkaDeliverMessage {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String content;
    private Long receivedAt;
    private int unreadCount;
    private Set<Long> readUsers; // optional
}
