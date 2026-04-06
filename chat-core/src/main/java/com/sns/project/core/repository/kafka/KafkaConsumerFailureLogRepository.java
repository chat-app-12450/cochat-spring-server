package com.sns.project.core.repository.kafka;

import com.sns.project.core.domain.kafka.KafkaConsumerFailureLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KafkaConsumerFailureLogRepository extends JpaRepository<KafkaConsumerFailureLog, Long> {
}
