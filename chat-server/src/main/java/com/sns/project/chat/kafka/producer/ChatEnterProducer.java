package com.sns.project.chat.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.sns.project.chat.kafka.dto.request.KafkaChatEnterRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatEnterProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(KafkaChatEnterRequest request){
        kafkaTemplate.send("chat-enter", request.getRoomId().toString(), request);
    }
}