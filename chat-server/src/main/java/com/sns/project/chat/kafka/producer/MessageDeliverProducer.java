package com.sns.project.chat.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.sns.project.chat.kafka.dto.request.KafkaDeliverMessage;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MessageDeliverProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;



    public void sendDeliver(KafkaDeliverMessage event) {
        kafkaTemplate.send("message.deliver", event.getRoomId().toString(), event);
    }
}
