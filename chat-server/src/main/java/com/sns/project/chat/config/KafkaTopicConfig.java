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

  @Value("${app.kafka.topics.chat-enter}")
  private String chatEnterTopicName;

  @Value("${app.kafka.topics.message-received}")
  private String messageReceivedTopicName;

  @Value("${app.kafka.topics.message-broadcast}")
  private String messageBroadcastTopicName;

  @Value("${app.kafka.topics.message-vector}")
  private String messageVectorTopicName;

  // 채팅방 입장 처리용
  @Bean
  public NewTopic chatEnterTopic() {
    // 개발 환경에서는 로컬 Kafka가 비어 있는 경우가 많아서 토픽을 자동 생성해둔다.
    // 운영 환경에서는 이미 만들어진 토픽 설정을 쓰도록 dev 프로필에서만 이 설정을 적용한다.
    return TopicBuilder.name(chatEnterTopicName)
        .partitions(6)
        .replicas(1)
        .build();
  }


  @Bean
  public NewTopic messageReceivedTopic() {
    return TopicBuilder.name(messageReceivedTopicName)
        .partitions(6)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic messageBroadcastTopic() {
    return TopicBuilder.name(messageBroadcastTopicName)
        .partitions(6)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic messageVectorTopic() {
    return TopicBuilder.name(messageVectorTopicName)
        .partitions(6)
        .replicas(1)
        .build();
  }
}
