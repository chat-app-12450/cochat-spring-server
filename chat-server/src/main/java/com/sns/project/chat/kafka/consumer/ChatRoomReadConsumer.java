package com.sns.project.chat.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.chat.service.ChatRealtimeStateService;
import com.sns.project.core.kafka.dto.event.ChatRoomReadKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomReadConsumer {

    private final ObjectMapper objectMapper;
    private final ChatRealtimeStateService chatRealtimeStateService;

    // 사용자가 방을 읽었다는 원본 이벤트를 읽고 Redis unread projection을 비운다.
    // message.created unread consumer와 같은 역할 축이므로 groupId를 chat-unread로 맞춘다.
    @KafkaListener(
        topics = "${app.kafka.topics.chat-room-read}",
        groupId = "chat-unread",
        containerFactory = "outboxStringKafkaListenerContainerFactory"
    )
    public void consume(String payload, Acknowledgment ack) throws Exception {
        ChatRoomReadKafkaEvent readEvent = objectMapper.readValue(payload, ChatRoomReadKafkaEvent.class);
        chatRealtimeStateService.clearUnreadCount(readEvent.getRoomId(), readEvent.getUserId());

        log.info("room-read projection applied from outbox: roomId={}, userId={}",
            readEvent.getRoomId(), readEvent.getUserId());
        ack.acknowledge();
    }
}
