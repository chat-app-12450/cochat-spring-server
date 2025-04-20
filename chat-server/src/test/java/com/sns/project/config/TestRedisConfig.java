package com.sns.project.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;

@TestConfiguration
public class TestRedisConfig {
  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
    redisConfig.setHostName("localhost");
    redisConfig.setPort(6380);  // ✅ 강제적으로 6380 사용
    return new LettuceConnectionFactory(redisConfig);
  }
}
