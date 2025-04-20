package com.sns.project.chat.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.chat.kafka.dto.request.KafkaChatEnterDeliverRequest;
import com.sns.project.chat.kafka.dto.request.KafkaChatEnterRequest;
import com.sns.project.chat.kafka.processor.ChatEnterProcessor;
import com.sns.project.chat.kafka.producer.ChatEnterDeliverProducer;
import com.sns.project.chat.service.ChatRedisService;
import com.sns.project.chat.service.ChatPresenceService;
import com.sns.project.chat.service.ChatRoomService;
import com.sns.project.chat.service.ChatService;
import com.sns.project.chat.service.dto.LastReadIdInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j   
public class ChatEnterConsumer {

   private final ObjectMapper objectMapper;
   private final ChatEnterProcessor chatEnterProcessor;
   private final ChatEnterDeliverProducer chatEnterDeliverProducer;

   @KafkaListener(topics = "chat-enter", groupId = "chat-enter-group")
   public void consume(String message, Acknowledgment ack) throws JsonProcessingException{
      KafkaChatEnterRequest request = objectMapper.readValue(message, KafkaChatEnterRequest.class);
      KafkaChatEnterDeliverRequest deliverRequest = chatEnterProcessor.process(request);
      chatEnterDeliverProducer.deliver(deliverRequest);
      ack.acknowledge();
   }
}
