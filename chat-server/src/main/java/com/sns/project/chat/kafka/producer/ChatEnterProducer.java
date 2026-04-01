package com.sns.project.chat.kafka.producer;

import com.sns.project.core.kafka.dto.request.KafkaChatEnterRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatEnterProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.chat-enter}")
    private String chatEnterTopicName;

    // chat.enter 
    public void send(KafkaChatEnterRequest request){
        kafkaTemplate.send(chatEnterTopicName, request.getRoomId().toString(), request);
    }
}
