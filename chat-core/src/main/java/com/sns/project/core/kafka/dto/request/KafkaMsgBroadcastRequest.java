package com.sns.project.core.kafka.dto.request;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMsgBroadcastRequest {
  private Long messageId;
  private Long roomId;
  private Long senderId;
  private String content;
  private Long receivedAt;
}