package com.sns.project.chat_consumer.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@Data
@AllArgsConstructor  // <- 추가
@NoArgsConstructor  // <- 추가
public class KafkaVectorMsgRequest {
    private Long msgId;
    private Long roomId;
    private String content;
    private Long senderId;
    private Long timestamp;
}
