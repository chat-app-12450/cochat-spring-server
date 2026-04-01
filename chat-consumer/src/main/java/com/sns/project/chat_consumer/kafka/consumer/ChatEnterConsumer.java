package com.sns.project.chat_consumer.kafka.consumer;

import com.sns.project.core.kafka.dto.request.KafkaChatEnterRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatEnterConsumer {


    @KafkaListener(
        topics = "${app.kafka.topics.chat-enter}",
        groupId = "chat-enter-group"
    )
    public void consume(KafkaChatEnterRequest request, Acknowledgment ack) throws JsonProcessingException {
        log.info("🎯 카프카 메시지 수신: 사용자 {}님이 방 {}에 입장", request.getUserId(), request.getRoomId());
        ack.acknowledge();
    }
} 
