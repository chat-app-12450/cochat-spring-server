package com.sns.project.chat_consumer.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DefaultErrorHandler;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class KafkaConsumerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    
    // 로깅 추가: 실제 사용되는 부트스트랩 서버 주소 확인
    log.info("🔍 설정된 Kafka 부트스트랩 서버 주소: {}", bootstrapServers);
    
    // 서버 주소를 properties에서 가져온 값 사용
    log.info("🔍 실제 사용할 Kafka 부트스트랩 서버 주소: {}", bootstrapServers);
    
    // 어디서 메시지를 받을지
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    // 그룹 아이디
    // props.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-enter-group");
    // 처음 메시지를 받을 때 오프셋
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    
    // 자동 커밋 비활성화 (수동 Ack를 위해)
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    
    // 역직렬화 - 기본 문자열 직렬화 사용
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    
    log.info("🔍 Kafka 전체 속성: {}", props);
    
    return new DefaultKafkaConsumerFactory<>(props);
  }

  // @KafkaListener가 동작할 환경을 만들어 주는 공장
  // 어떤 컨슈머 공장을 사용하고 에러처리할지 정의
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    
    // 메시지 변환기 설정 제거 - 리스너에서 직접 ObjectMapper로 변환
    
    // Ack 모드를 MANUAL로 설정
    factory.getContainerProperties().setAckMode(AckMode.MANUAL);
    
    factory.setCommonErrorHandler(new DefaultErrorHandler((record, exception) -> {
      // Silence error
      log.error("🚨 Kafka 메시지 처리 중 에러 발생: {}", exception.getMessage(), exception);
    }));
    return factory;
  }
}