// MessageProducer.java
package com.sns.project.chat.kafka.producer;

import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import lombok.RequiredArgsConstructor;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class MessageProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NewTopic messageReceivedTopic;

    public void send(KafkaNewMsgRequest message) {
        kafkaTemplate.send(messageReceivedTopic.name(), message.getRoomId().toString(), message);
    }
}
