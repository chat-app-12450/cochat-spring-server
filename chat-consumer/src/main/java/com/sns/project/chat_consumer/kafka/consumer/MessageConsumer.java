package com.sns.project.chat_consumer.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import com.sns.project.chat_consumer.kafka.processor.MessageProcessor;
import com.sns.project.chat_consumer.kafka.producer.MessageBroadcastProducer;
import com.sns.project.chat_consumer.service.ChatRedisService;
import com.sns.project.chat_consumer.service.ChatService;
import com.sns.project.chat_consumer.service.UnreadCountService;
import com.sns.project.core.kafka.dto.request.KafkaMsgBroadcastRequest;
import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumer {

    private final ChatRedisService chatRedisService;
    private final ObjectMapper objectMapper;
    private final UnreadCountService unreadCountService;
    private final ChatService chatService;
    private final MessageBroadcastProducer messageBroadcastProducer;
    private final MessageProcessor messageProcessor;

    @KafkaListener(
        topics = "message.received",
        groupId = "message-received-group",
        containerFactory = "kafkaListenerContainerFactory")
    public void consume(String json, Acknowledgment ack) throws JsonProcessingException {
        KafkaNewMsgRequest message = objectMapper.readValue(json, KafkaNewMsgRequest.class);
        log.info("🎯 카프카 메시지 수신: 사용자 {}이 방 {}에 메시지 전송(내용: {})", message.getSenderId(), message.getRoomId(), message.getContent());
        KafkaMsgBroadcastRequest broadcastRequest = messageProcessor.process(message);
        messageBroadcastProducer.sendDeliver(broadcastRequest);
        ack.acknowledge();
    }
}