package com.sns.project.chat.kafka.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KafkaChatEnterRequest {
    private Long roomId;
    private Long userId;
}
