package com.sns.project.chat_consumer.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.sns.project.chat_consumer.kafka.dto.request.KafkaVectorMsgRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageVectorProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "message.vector";
    
    public void send(KafkaVectorMsgRequest message) {
        kafkaTemplate.send(TOPIC, message.getRoomId().toString(), message);
    }
}
