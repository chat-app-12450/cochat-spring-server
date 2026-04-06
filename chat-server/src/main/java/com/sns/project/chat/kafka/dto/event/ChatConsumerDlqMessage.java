package com.sns.project.chat.kafka.dto.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConsumerDlqMessage {

    private String consumerGroup;
    private String originalTopic;
    private String dlqTopic;
    private String messageKey;
    private Integer partition;
    private Long offset;
    private Integer retryCount;
    private String payload;
    private String errorMessage;
    private LocalDateTime failedAt;
}
