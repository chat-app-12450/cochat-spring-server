package com.sns.project.chat_consumer.kafka.dto.request;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class KafkaVectorMsgRequest {
    private Long msgId;
    private Long roomId;
    private String content;
    private Long senderId;
    private Long timestamp;
}
