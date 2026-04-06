package com.sns.project.chat.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.chat.kafka.dto.event.ChatConsumerDlqMessage;
import com.sns.project.core.domain.kafka.KafkaConsumerFailureLog;
import com.sns.project.core.repository.kafka.KafkaConsumerFailureLogRepository;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaDlqService {

    private final KafkaConsumerFailureLogRepository kafkaConsumerFailureLogRepository;
    private final ObjectMapper objectMapper;
    @Qualifier("outboxKafkaTemplate")
    private final KafkaTemplate<String, String> outboxKafkaTemplate;

    @Value("${app.kafka.topics.chat-dlq}")
    private String chatDlqTopic;

    @Transactional
    public void publishToDlq(
        String consumerGroup,
        ConsumerRecord<String, String> record,
        Exception exception,
        int retryCount
    ) {
        LocalDateTime failedAt = LocalDateTime.now();
        String errorMessage = buildErrorMessage(exception);
        String stackTrace = buildStackTrace(exception);

        kafkaConsumerFailureLogRepository.save(
            KafkaConsumerFailureLog.builder()
                .consumerGroup(consumerGroup)
                .originalTopic(record.topic())
                .dlqTopic(chatDlqTopic)
                .messageKey(record.key())
                .partitionNo(record.partition())
                .offsetNo(record.offset())
                .retryCount(retryCount)
                .payload(record.value())
                .errorMessage(errorMessage)
                .stackTrace(stackTrace)
                .failedAt(failedAt)
                .build()
        );

        try {
            ChatConsumerDlqMessage dlqMessage = ChatConsumerDlqMessage.builder()
                .consumerGroup(consumerGroup)
                .originalTopic(record.topic())
                .dlqTopic(chatDlqTopic)
                .messageKey(record.key())
                .partition(record.partition())
                .offset(record.offset())
                .retryCount(retryCount)
                .payload(record.value())
                .errorMessage(errorMessage)
                .failedAt(failedAt)
                .build();

            outboxKafkaTemplate.send(
                chatDlqTopic,
                buildDlqKey(consumerGroup, record),
                objectMapper.writeValueAsString(dlqMessage)
            ).get();
        } catch (Exception dlqPublishException) {
            log.error(
                "failed to publish DLQ message: consumerGroup={}, topic={}, partition={}, offset={}",
                consumerGroup,
                record.topic(),
                record.partition(),
                record.offset(),
                dlqPublishException
            );
            throw new IllegalStateException("DLQ publish failed", dlqPublishException);
        }
    }

    private String buildDlqKey(String consumerGroup, ConsumerRecord<String, String> record) {
        return consumerGroup + ":" + record.topic() + ":" + record.partition() + ":" + record.offset();
    }

    private String buildErrorMessage(Exception exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getClass().getSimpleName() + ": " + rootCause.getMessage();
    }

    private String buildStackTrace(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
