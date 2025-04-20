
package com.sns.project.chat.kafka.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KafkaChatEnterRequest {
    private Long roomId;
    private Long userId;
}
