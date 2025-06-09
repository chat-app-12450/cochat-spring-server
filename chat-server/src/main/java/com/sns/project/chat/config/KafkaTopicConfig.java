package com.sns.project.chat.config;

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
  public NewTopic messageReceivedTopic() {
    return TopicBuilder.name("message.received")
        .partitions(6)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic messageDeliverTopic() {
    return TopicBuilder.name("message.deliver")
        .partitions(6)
        .replicas(1)
        .build();
  }
}
