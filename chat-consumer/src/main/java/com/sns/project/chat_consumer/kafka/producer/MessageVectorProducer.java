package com.sns.project.chat_consumer.kafka.producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.sns.project.chat_consumer.dto.request.KafkaVectorMsgRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageVectorProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NewTopic messageVectorTopic;

    public void send(KafkaVectorMsgRequest message) {
        kafkaTemplate.send(messageVectorTopic.name(), message.getRoomId().toString(), message);
    }
}
