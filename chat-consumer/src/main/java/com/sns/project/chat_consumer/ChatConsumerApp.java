package com.sns.project.chat_consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.sns.project.chat_consumer", "com.sns.project.core"})
@EntityScan(basePackages = "com.sns.project.core.domain")
@EnableJpaRepositories(basePackages = "com.sns.project.core.repository")
public class ChatConsumerApp {
    public static void main(String[] args) {
        SpringApplication.run(ChatConsumerApp.class, args);
    }
}

