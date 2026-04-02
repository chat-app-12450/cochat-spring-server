package com.sns.project.core.kafka.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomReadKafkaEvent {
    private Long roomId;
    private Long userId;
    private Long messageId;
}
