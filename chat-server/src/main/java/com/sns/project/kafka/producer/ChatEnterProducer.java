
package com.sns.project.kafka.producer;

import com.sns.project.core.kafka.dto.request.KafkaChatEnterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatEnterProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "chat.enter";

    public void send(KafkaChatEnterRequest event) {
        log.info("🚪 입장 이벤트 발행 요청: roomId={}, userId={}",
            event.getRoomId(), event.getUserId());

        try {
            kafkaTemplate.send(TOPIC,
                    event.getRoomId().toString(), // key: roomId
                    event)
                .get(); // 동기 전송
            log.info("✅ 입장 이벤트 Produce 성공");
        } catch (Exception e) {
            log.error("❌ 입장 이벤트 Produce 실패", e);
        }
    }
}
