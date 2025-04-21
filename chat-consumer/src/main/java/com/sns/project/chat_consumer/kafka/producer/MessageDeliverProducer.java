package com.sns.project.chat_consumer.kafka.producer;


import com.sns.project.chat_consumer.kafka.dto.request.KafkaDeliverMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MessageDeliverProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;



  public void sendDeliver(KafkaDeliverMessage event) {
    kafkaTemplate.send("message.deliver", event.getRoomId().toString(), event);
  }
}
