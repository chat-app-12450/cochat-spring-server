package com.sns.project.chat.kafka.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaChatEnterDeliverRequest {
    private Long prevLastReadId;
    private Long roomId;
    private Long userId;
    private Long newLastReadId;
}
