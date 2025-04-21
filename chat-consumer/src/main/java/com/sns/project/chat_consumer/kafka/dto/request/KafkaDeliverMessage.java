package com.sns.project.chat_consumer.kafka.dto.request;

import java.util.Set;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaDeliverMessage {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String content;
    private Long receivedAt;
    private int unreadCount;
    private Set<Long> readUsers; // optional
} 