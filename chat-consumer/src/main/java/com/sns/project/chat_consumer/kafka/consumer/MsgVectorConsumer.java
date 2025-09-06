package com.sns.project.chat_consumer.kafka.consumer;

import com.sns.project.chat_consumer.kafka.consumer.processor.MsgVectorProcessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.chat_consumer.dto.request.KafkaVectorMsgRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.common.xcontent.XContentType;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MsgVectorConsumer {

    // private final ObjectMapper objectMapper;
    private final MsgVectorProcessor msgVectorProcessor;
    @KafkaListener(
        topics = "message.vector",
        groupId = "message-vector-group"
        // properties = {
            // "spring.json.value.default.type=com.sns.project.chat_consumer.dto.request.KafkaVectorMsgRequest"
        // }
        // containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(KafkaVectorMsgRequest message, Acknowledgment ack) throws JsonProcessingException {
        // KafkaVectorMsgRequest message = objectMapper.readValue(json, KafkaVectorMsgRequest.class);
        log.info("🎯 카프카 메시지 opensearch 저장: 사용자 {}이 방 {}에 메시지 전송(내용: {})", message.getSenderId(), message.getRoomId(), message.getContent());
        msgVectorProcessor.process(message);
        ack.acknowledge();
    }
}