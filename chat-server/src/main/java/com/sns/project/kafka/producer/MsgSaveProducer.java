package com.sns.project.kafka.producer;


import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
@Slf4j
public class MsgSaveProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  private static final String TOPIC = "message.save";

  public void sendDeliver(KafkaNewMsgRequest event) {
    log.info("💙 메시지 save 요청 도착");

    try {
      kafkaTemplate.send(TOPIC, event.getRoomId().toString(), event).get();
      log.info("✅ Produce 성공 roomId={}, sender={}",
          event.getRoomId(), event.getSenderId());
    } catch (Exception e) {
      log.error("❌ Produce 실패", e);
    }
  }
}
