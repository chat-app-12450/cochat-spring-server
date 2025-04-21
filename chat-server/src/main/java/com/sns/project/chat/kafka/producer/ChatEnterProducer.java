package com.sns.project.chat.kafka.producer;

import com.sns.project.core.kafka.dto.request.KafkaChatEnterRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatEnterProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(KafkaChatEnterRequest request){
        kafkaTemplate.send("chat-enter", request.getRoomId().toString(), request);
    }
}