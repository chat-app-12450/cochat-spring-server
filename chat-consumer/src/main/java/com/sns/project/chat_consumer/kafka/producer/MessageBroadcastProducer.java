package com.sns.project.chat_consumer.kafka.producer;


import com.sns.project.core.kafka.dto.request.KafkaMsgBroadcastRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MessageBroadcastProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;



  public void sendDeliver(KafkaMsgBroadcastRequest event) {
    kafkaTemplate.send("message.broadcast", event.getRoomId().toString(), event);
  }
}
