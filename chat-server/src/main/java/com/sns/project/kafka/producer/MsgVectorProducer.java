package com.sns.project.kafka.producer;

import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MsgVectorProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "message.vector";

    public void send(KafkaNewMsgRequest event) {
        log.info("🧊 벡터 생성 이벤트 발행 요청: roomId={}, sender={}",
            event.getRoomId(), event.getSenderId());

        try {
            kafkaTemplate.send(TOPIC, event.getRoomId().toString(), event).get();
            log.info("✅ 벡터 이벤트 Produce 성공");
        } catch (Exception e) {
            log.error("❌ 벡터 이벤트 Produce 실패", e);
        }
    }
}
