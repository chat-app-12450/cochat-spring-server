package com.sns.project.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

  // 채팅방 입장 처리용
  @Bean
  public NewTopic chatEnterTopic() {
    return TopicBuilder.name("chat.enter")
        .partitions(6)
        .replicas(1)
        .build();
  }




  @Bean
  public NewTopic messageBroadcastTopic() {
    return TopicBuilder.name("message.save")
        .partitions(6)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic messageVectorTopic() {
    return TopicBuilder.name("message.vector")
        .partitions(6)
        .replicas(1)
        .build();
  }
}
