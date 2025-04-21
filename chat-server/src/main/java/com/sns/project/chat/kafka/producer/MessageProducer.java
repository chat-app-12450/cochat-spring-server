// MessageProducer.java
package com.sns.project.chat.kafka.producer;

import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class MessageProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "message.received";

    public void send(KafkaNewMsgRequest message) {
        kafkaTemplate.send(TOPIC, message.getRoomId().toString(), message);
    }
}
