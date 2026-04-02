package com.sns.project.chat.outbox;

import com.sns.project.core.domain.outbox.OutboxEvent;
import com.sns.project.core.domain.outbox.OutboxStatus;
import com.sns.project.core.repository.outbox.OutboxEventRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OutboxRelayService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> outboxKafkaTemplate;

    @Value("${app.outbox.relay.batch-size:100}")
    private int batchSize;

    @Value("${app.outbox.relay.max-retry-count:10}")
    private int maxRetryCount;

    public OutboxRelayService(
        OutboxEventRepository outboxEventRepository,
        @Qualifier("outboxKafkaTemplate") KafkaTemplate<String, String> outboxKafkaTemplate
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxKafkaTemplate = outboxKafkaTemplate;
    }

    @Transactional
    public void relayPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findAllByStatusOrderByIdAsc(
            OutboxStatus.PENDING,
            PageRequest.of(0, batchSize)
        );

        for (OutboxEvent outboxEvent : pendingEvents) {
            try {
                outboxKafkaTemplate
                    .send(outboxEvent.getTopic(), outboxEvent.getEventKey(), outboxEvent.getPayload())
                    // send비동기기 때문에 get으로 기다려야함.
                    .get();
                outboxEvent.markPublished();
            } catch (Exception e) {
                outboxEvent.markPublishFailed(maxRetryCount);
                log.warn(
                    "outbox publish failed: id={}, eventType={}, topic={}, retryCount={}",
                    outboxEvent.getId(),
                    outboxEvent.getEventType(),
                    outboxEvent.getTopic(),
                    outboxEvent.getRetryCount() + 1,
                    e
                );
            }
        }
    }
}
