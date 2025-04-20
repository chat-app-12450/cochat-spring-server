package com.sns.project.chat.kafka;

import com.sns.project.chat.kafka.dto.request.KafkaChatEnterRequest;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public ConsumerFactory<String, KafkaChatEnterRequest> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    // 어디서 메시지를 받을지
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    // 그룹 아이디
    // props.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-enter-group");
    // 처음 메시지를 받을 때 오프셋
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    
    // 역직렬화
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
    
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, KafkaChatEnterRequest.class.getName());
    
    return new DefaultKafkaConsumerFactory<>(props);
  }

  // @KafkaListener가 동작할 환경을 만들어 주는 공장
  // 어떤 컨슈머 공장을 사용하고 에러처리할지 정의
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, KafkaChatEnterRequest> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, KafkaChatEnterRequest> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setCommonErrorHandler(new DefaultErrorHandler((record, exception) -> {
      // Silence error
      System.out.println("🚨 에러 발생: " + exception.getMessage());
    }));
    return factory;
  }
}