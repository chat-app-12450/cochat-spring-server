package com.sns.project.chat_consumer.kafka.consumer.processor;

import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MsgVectorProcessor {

  @Value("${opensearch.index.chat.name}")
  private String indexName;
  private final RestHighLevelClient openSearchClient;

  public void process(KafkaNewMsgRequest message){
    // OpenSearch에 저장
    Map<String, Object> doc = new HashMap<>();
    doc.put("id", message.getMessageId());
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
  }

}
