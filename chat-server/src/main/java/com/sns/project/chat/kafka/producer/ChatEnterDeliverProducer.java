package com.sns.project.chat.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.sns.project.chat.kafka.dto.request.KafkaChatEnterDeliverRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatEnterDeliverProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void deliver(KafkaChatEnterDeliverRequest request) {
        kafkaTemplate.send("chat-enter-deliver", request);
    }
}
