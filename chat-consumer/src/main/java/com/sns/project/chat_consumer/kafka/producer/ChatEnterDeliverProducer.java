package com.sns.project.chat_consumer.kafka.producer;

import com.sns.project.chat_consumer.kafka.dto.request.KafkaChatEnterDeliverRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatEnterDeliverProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void deliver(KafkaChatEnterDeliverRequest request) {
    kafkaTemplate.send("chat-enter-deliver", request);
  }
}