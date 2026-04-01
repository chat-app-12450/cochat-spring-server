package com.sns.project.chat.kafka.producer;

import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageVectorProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.message-vector}")
    private String messageVectorTopicName;

    public void send(KafkaNewMsgRequest message) {
        kafkaTemplate.send(messageVectorTopicName, message.getRoomId().toString(), message);
    }
}
