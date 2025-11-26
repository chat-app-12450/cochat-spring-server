package com.sns.project.core.kafka.dto.request;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class KafkaNewMsgRequest {
    private Long roomId;
    private Long senderId;
    private String content;
    private Long receivedAt;
    // private String clientMessageId;
    private Long messageId;
} 