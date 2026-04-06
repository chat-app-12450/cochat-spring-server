package com.sns.project.chat.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.chat.service.ChatRealtimeStateService;
import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageCreatedUnreadConsumer {

    private final ObjectMapper objectMapper;
    private final ChatRealtimeStateService chatRealtimeStateService;

    // 같은 message.created 원본 이벤트를 unread projection 역할도 별도 consumer group으로 소비한다.
    // 방 안에 없는 참가자만 Redis unread count를 올리고, sender 자신은 제외한다.
    @KafkaListener(
        topics = "${app.kafka.topics.chat-message-created}",
        groupId = "chat-unread",
        containerFactory = "chatUnreadKafkaListenerContainerFactory"
    )
    public void consume(String payload, Acknowledgment ack) throws Exception {
        KafkaNewMsgRequest unreadEvent = objectMapper.readValue(payload, KafkaNewMsgRequest.class);
        chatRealtimeStateService.incrementUnreadCounts(unreadEvent.getRoomId(), unreadEvent.getSenderId());

        log.info("unread projection updated from outbox: roomId={}, senderId={}",
            unreadEvent.getRoomId(), unreadEvent.getSenderId());
        ack.acknowledge();
    }
}
