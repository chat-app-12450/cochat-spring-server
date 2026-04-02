package com.sns.project.chat.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class OutboxKafkaConfig {

    // 프로듀서가 브로커에 저장할때 객체를 어떻게 직렬화할지
    @Bean
    public ProducerFactory<String, String> outboxProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = new HashMap<>(kafkaProperties.buildProducerProperties());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, String> outboxKafkaTemplate(ProducerFactory<String, String> outboxProducerFactory) {
        // Outbox 전용 producer 빈.
        // OutboxRelayService는 이 KafkaTemplate으로 chat.message.created / chat.room.read 토픽에 발행한다.
        return new KafkaTemplate<>(outboxProducerFactory);
    }
}
