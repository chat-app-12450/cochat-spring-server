CREATE TABLE IF NOT EXISTS kafka_consumer_failure_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    consumer_group VARCHAR(100) NOT NULL,
    original_topic VARCHAR(200) NOT NULL,
    dlq_topic VARCHAR(200) NOT NULL,
    message_key VARCHAR(200),
    partition_no INT NOT NULL,
    offset_no BIGINT NOT NULL,
    retry_count INT NOT NULL,
    payload TEXT NOT NULL,
    error_message TEXT NOT NULL,
    stack_trace TEXT,
    failed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_kafka_failure_group_topic_time (consumer_group, original_topic, failed_at),
    KEY idx_kafka_failure_topic_partition_offset (original_topic, partition_no, offset_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
