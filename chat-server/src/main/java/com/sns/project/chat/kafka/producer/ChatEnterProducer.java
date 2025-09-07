package com.sns.project.chat.kafka.producer;

import com.sns.project.core.kafka.dto.request.KafkaChatEnterRequest;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatEnterProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NewTopic chatEnterTopic;

    // chat.enter 
    public void send(KafkaChatEnterRequest request){
        kafkaTemplate.send(chatEnterTopic.name(), request.getRoomId().toString(), request);
    }
}