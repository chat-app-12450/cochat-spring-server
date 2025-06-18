package com.sns.project.chat_consumer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;

@Configuration
public class KafkaTopicConfig {


    
    
    @Bean
    public NewTopic messageVectorTopic() {
        return TopicBuilder.name("message.vector")
            .partitions(6)
            .replicas(1)
            .build();
    }
}
