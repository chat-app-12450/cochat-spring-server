package com.sns.project.chat.config;

import com.sns.project.chat.kafka.KafkaDlqService;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.util.backoff.FixedBackOff;

// 컨슈머가 사용할 설정
@Configuration
public class OutboxKafkaListenerConfig {

    // 브로커에서 꺼낸 바이트코드를 어떻게 역직렬화하여 읽을지
    @Bean
    public ConsumerFactory<String, String> outboxStringConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = new HashMap<>(kafkaProperties.buildConsumerProperties());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> outboxStringKafkaListenerContainerFactory(
        ConsumerFactory<String, String> outboxStringConsumerFactory
    ) {
        return buildFactory(outboxStringConsumerFactory, null);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> chatUnreadKafkaListenerContainerFactory(
        ConsumerFactory<String, String> outboxStringConsumerFactory,
        @Qualifier("chatUnreadKafkaErrorHandler") CommonErrorHandler chatUnreadKafkaErrorHandler
    ) {
        return buildFactory(outboxStringConsumerFactory, chatUnreadKafkaErrorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> chatBroadcastKafkaListenerContainerFactory(
        ConsumerFactory<String, String> outboxStringConsumerFactory,
        @Qualifier("chatBroadcastKafkaErrorHandler") CommonErrorHandler chatBroadcastKafkaErrorHandler
    ) {
        return buildFactory(outboxStringConsumerFactory, chatBroadcastKafkaErrorHandler);
    }

    @Bean
    public CommonErrorHandler chatUnreadKafkaErrorHandler(
        KafkaDlqService kafkaDlqService,
        @Value("${app.kafka.consumer.max-retry-count:3}") int maxRetryCount,
        @Value("${app.kafka.consumer.retry-backoff-ms:1000}") long retryBackoffMs
    ) {
        return buildErrorHandler("chat-unread", kafkaDlqService, maxRetryCount, retryBackoffMs);
    }

    @Bean
    public CommonErrorHandler chatBroadcastKafkaErrorHandler(
        KafkaDlqService kafkaDlqService,
        @Value("${app.kafka.consumer.max-retry-count:3}") int maxRetryCount,
        @Value("${app.kafka.consumer.retry-backoff-ms:1000}") long retryBackoffMs
    ) {
        return buildErrorHandler("chat-broadcast", kafkaDlqService, maxRetryCount, retryBackoffMs);
    }

    private ConcurrentKafkaListenerContainerFactory<String, String> buildFactory(
        ConsumerFactory<String, String> consumerFactory,
        CommonErrorHandler commonErrorHandler
    ) {
        // value를 String으로 받고, 처리 성공 후에만 ack 하도록 manual ack 모드로 맞춘다.
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setDeliveryAttemptHeader(true);
        if (commonErrorHandler != null) {
            factory.setCommonErrorHandler(commonErrorHandler);
        }
        return factory;
    }

    private CommonErrorHandler buildErrorHandler(
        String consumerGroup,
        KafkaDlqService kafkaDlqService,
        int maxRetryCount,
        long retryBackoffMs
    ) {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            (record, exception) -> kafkaDlqService.publishToDlq(
                consumerGroup,
                castRecord(record),
                exception,
                resolveRetryCount(record, maxRetryCount)
            ),
            new FixedBackOff(retryBackoffMs, maxRetryCount)
        );
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }

    @SuppressWarnings("unchecked")
    private ConsumerRecord<String, String> castRecord(ConsumerRecord<?, ?> record) {
        return (ConsumerRecord<String, String>) record;
    }

    private int resolveRetryCount(ConsumerRecord<?, ?> record, int fallbackRetryCount) {
        var header = record.headers().lastHeader(KafkaHeaders.DELIVERY_ATTEMPT);
        if (header == null || header.value() == null || header.value().length < Integer.BYTES) {
            return fallbackRetryCount;
        }
        return Math.max(ByteBuffer.wrap(header.value()).getInt() - 1, 0);
    }
}
