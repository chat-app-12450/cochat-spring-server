package com.sns.project.chat_consumer.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.chat_consumer.kafka.dto.request.KafkaVectorMsgRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MsgVectorConsumer {

    private final ObjectMapper objectMapper;
    private final RestHighLevelClient openSearchClient;

//    @Value("${opensearch.index.name:chat-index}")
    private String indexName = "chat-index";

    @KafkaListener(
        topics = "message.vector",
        groupId = "message-vector-group",
        containerFactory = "kafkaListenerContainerFactory")
    public void consume(String json, Acknowledgment ack) throws JsonProcessingException {
        KafkaVectorMsgRequest message = objectMapper.readValue(json, KafkaVectorMsgRequest.class);
        log.info("🎯 카프카 메시지 opensearch 저장: 사용자 {}이 방 {}에 메시지 전송(내용: {})", message.getSenderId(), message.getRoomId(), message.getContent());

        // OpenSearch에 저장
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", message.getMsgId());
        doc.put("chat_content", message.getContent());
        doc.put("timestamp", System.currentTimeMillis());
        doc.put("user", message.getSenderId());

        IndexRequest request = new IndexRequest(indexName)
            .source(doc, XContentType.JSON);

        try {
            IndexResponse response = openSearchClient.index(request, RequestOptions.DEFAULT);
            log.info("OpenSearch 저장 결과: {}", response.getResult());
        } catch (Exception e) {
            log.error("OpenSearch 저장 실패", e);
        }

        ack.acknowledge();
    }
}