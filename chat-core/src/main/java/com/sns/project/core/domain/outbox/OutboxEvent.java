package com.sns.project.core.domain.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "outbox_event",
    indexes = {
        @Index(name = "idx_outbox_event_status_id", columnList = "status, id"),
        @Index(name = "idx_outbox_event_event_type", columnList = "event_type")
    }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private OutboxAggregateType aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private OutboxEventType eventType;

    @Column(nullable = false, length = 200)
    private String topic;

    @Column(name = "event_key", nullable = false, length = 100)
    private String eventKey;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static OutboxEvent pending(
        OutboxAggregateType aggregateType,
        Long aggregateId,
        OutboxEventType eventType,
        String topic,
        String eventKey,
        String payload
    ) {
        return OutboxEvent.builder()
            .aggregateType(aggregateType)
            .aggregateId(aggregateId)
            .eventType(eventType)
            .topic(topic)
            .eventKey(eventKey)
            .payload(payload)
            .status(OutboxStatus.PENDING)
            .retryCount(0)
            .build();
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = OutboxStatus.PENDING;
        }
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markPublishFailed(int maxRetryCount) {
        this.retryCount += 1;
        if (this.retryCount >= maxRetryCount) {
            this.status = OutboxStatus.FAILED;
        }
    }
}
