package com.sns.project.core.kafka.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class KafkaChatEnterRequest {
    private Long roomId;
    private Long userId;
} 