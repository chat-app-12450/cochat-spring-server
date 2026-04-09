package com.sns.project.chat.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sns.project.chat.service.ChatRealtimeStateService;
import com.sns.project.chat.websocket.dto.ChatReadReceiptBroadcast;
import com.sns.project.core.kafka.dto.event.ChatRoomReadKafkaEvent;
import com.sns.project.core.repository.chat.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomReadConsumer {

    private final ObjectMapper objectMapper;
    private final ChatRealtimeStateService chatRealtimeStateService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    // 사용자가 방을 읽었다는 원본 이벤트를 읽고 Redis unread projection을 비운다.
    // message.created unread consumer와 같은 역할 축이므로 groupId를 chat-unread로 맞춘다.
    @KafkaListener(
        topics = "${app.kafka.topics.chat-room-read}",
        groupId = "chat-unread",
        containerFactory = "chatUnreadKafkaListenerContainerFactory"
    )
    public void consume(String payload, Acknowledgment ack) throws Exception {
        ChatRoomReadKafkaEvent readEvent = objectMapper.readValue(payload, ChatRoomReadKafkaEvent.class);
        Long previousReadSeq = readEvent.getPreviousReadSeq() != null ? readEvent.getPreviousReadSeq() : 0L;

        chatMessageRepository.decrementUnreadCountInRange(
            readEvent.getRoomId(),
            readEvent.getUserId(),
            previousReadSeq,
            readEvent.getNewReadSeq()
        );
        chatRealtimeStateService.clearUnreadCount(readEvent.getRoomId(), readEvent.getUserId());

        ChatReadReceiptBroadcast receipt = ChatReadReceiptBroadcast.builder()
            .roomId(readEvent.getRoomId())
            .readerId(readEvent.getUserId())
            .previousReadSeq(previousReadSeq)
            .newReadSeq(readEvent.getNewReadSeq())
            .build();
        messagingTemplate.convertAndSend("/topic/chat/rooms/" + readEvent.getRoomId(), receipt);

        log.info("room-read projection applied from outbox: roomId={}, userId={}, previousReadSeq={}, newReadSeq={}",
            readEvent.getRoomId(), readEvent.getUserId(), previousReadSeq, readEvent.getNewReadSeq());
        ack.acknowledge();
    }
}
