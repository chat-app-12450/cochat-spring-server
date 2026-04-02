package com.sns.project.chat.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("dev")
public class KafkaTopicConfig {

  @Value("${app.kafka.topics.chat-message-created}")
  private String chatMessageCreatedTopicName;

  @Value("${app.kafka.topics.chat-room-read}")
  private String chatRoomReadTopicName;

  @Bean
  public NewTopic chatMessageCreatedTopic() {
    return TopicBuilder.name(chatMessageCreatedTopicName)
        .partitions(6)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic chatRoomReadTopic() {
    return TopicBuilder.name(chatRoomReadTopicName)
        .partitions(6)
        .replicas(1)
        .build();
  }
}
