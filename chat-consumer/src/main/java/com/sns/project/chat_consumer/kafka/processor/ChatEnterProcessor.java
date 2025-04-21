package com.sns.project.chat_consumer.kafka.processor;

import com.sns.project.chat_consumer.kafka.dto.request.KafkaChatEnterDeliverRequest;
import com.sns.project.chat_consumer.service.ChatPresenceService;
import com.sns.project.chat_consumer.service.ChatService;
import com.sns.project.chat_consumer.service.dto.LastReadIdInfo;
import com.sns.project.core.kafka.dto.request.KafkaChatEnterRequest;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEnterProcessor {
    private final ChatPresenceService chatPresenceService;
    private final ChatService chatService;

    public KafkaChatEnterDeliverRequest process(KafkaChatEnterRequest request) {
        chatPresenceService.userEnteredRoom(request.getRoomId(), request.getUserId());
        LastReadIdInfo result = chatService.readAllMessages(request.getUserId(), request.getRoomId());

        log.info("🍉 user {} joined room {}", request.getUserId(), request.getRoomId());
        log.info("🍉 prevLastReadId: {}", result.getPrevLastReadId());
        log.info("🍉 newLastReadId: {}", result.getNewLastReadId());

        KafkaChatEnterDeliverRequest deliverRequest = KafkaChatEnterDeliverRequest.builder()
            .roomId(request.getRoomId())
            .userId(request.getUserId())
            .newLastReadId(result.getNewLastReadId())
            .prevLastReadId(result.getPrevLastReadId())
            .build();

        return deliverRequest;
    }
}