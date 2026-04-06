package com.sns.project.core.domain.kafka;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "kafka_consumer_failure_log",
    indexes = {
        @Index(name = "idx_kafka_failure_group_topic_time", columnList = "consumer_group, original_topic, failed_at"),
        @Index(name = "idx_kafka_failure_topic_partition_offset", columnList = "original_topic, partition_no, offset_no")
    }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaConsumerFailureLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consumer_group", nullable = false, length = 100)
    private String consumerGroup;

    @Column(name = "original_topic", nullable = false, length = 200)
    private String originalTopic;

    @Column(name = "dlq_topic", nullable = false, length = 200)
    private String dlqTopic;

    @Column(name = "message_key", length = 200)
    private String messageKey;

    @Column(name = "partition_no", nullable = false)
    private Integer partitionNo;

    @Column(name = "offset_no", nullable = false)
    private Long offsetNo;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Lob
    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    @Lob
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "failed_at", nullable = false, updatable = false)
    private LocalDateTime failedAt;

    @PrePersist
    void prePersist() {
        if (failedAt == null) {
            failedAt = LocalDateTime.now();
        }
    }
}
