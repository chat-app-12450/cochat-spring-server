CREATE TABLE kafka_consumer_failure_log (
    id BIGSERIAL PRIMARY KEY,
    consumer_group VARCHAR(100) NOT NULL,
    original_topic VARCHAR(200) NOT NULL,
    dlq_topic VARCHAR(200) NOT NULL,
    message_key VARCHAR(200),
    partition_no INTEGER NOT NULL,
    offset_no BIGINT NOT NULL,
    retry_count INTEGER NOT NULL,
    payload TEXT NOT NULL,
    error_message TEXT NOT NULL,
    stack_trace TEXT,
    failed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_kafka_failure_group_topic_time
    ON kafka_consumer_failure_log (consumer_group, original_topic, failed_at);

CREATE INDEX idx_kafka_failure_topic_partition_offset
    ON kafka_consumer_failure_log (original_topic, partition_no, offset_no);
