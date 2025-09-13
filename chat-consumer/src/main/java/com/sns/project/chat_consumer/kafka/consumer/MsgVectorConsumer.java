package com.sns.project.chat_consumer.kafka.consumer;

import com.sns.project.chat_consumer.kafka.consumer.processor.MsgVectorProcessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MsgVectorConsumer {

    // private final ObjectMapper objectMapper;
    private final MsgVectorProcessor msgVectorProcessor;
    @KafkaListener(
        topics = "message.vector",
        groupId = "message-vector-group"
    )
    public void consume(KafkaNewMsgRequest message, Acknowledgment ack) throws JsonProcessingException {
        log.info("🎯 카프카 메시지 opensearch 저장: 사용자 {}이 방 {}에 메시지 전송(내용: {})", message.getSenderId(), message.getRoomId(), message.getContent());
        // msgVectorProcessor.process(message);
        log.info("나중에......");
        ack.acknowledge();
    }
}