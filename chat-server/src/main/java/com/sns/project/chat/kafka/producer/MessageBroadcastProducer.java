package com.sns.project.chat.kafka.producer;

import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class MessageBroadcastProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Value("${app.kafka.topics.message-broadcast}")
  private String messageBroadcastTopicName;


  public void sendDeliver(KafkaNewMsgRequest event) {
    log.info("💙 메시지 브로드캐스트 요청 도착");
    try {
      kafkaTemplate.send(messageBroadcastTopicName, event.getRoomId().toString(), event).get();
      log.info("✅ Produce 성공");
    } catch (Exception e) {
      log.error("❌ Produce 실패", e);
    }

  }
}
