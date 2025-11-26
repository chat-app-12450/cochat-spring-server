package com.sns.project.chat_consumer.kafka.consumer;


import com.sns.project.core.domain.chat.ChatRoom;
import com.sns.project.core.domain.user.User;
import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import com.sns.project.core.domain.chat.ChatMessage;

import com.sns.project.core.repository.chat.ChatMessageRepository;
import com.sns.project.core.repository.chat.ChatRoomRepository;
import com.sns.project.core.repository.user.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MsgSaveConsumer {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @KafkaListener(
        topics = "message.save",
        groupId = "message-save-worker"   // HPA 확장하려면 동일 그룹 유지
    )
    public void consume(KafkaNewMsgRequest event, Acknowledgment ack) {
        try {
            log.info("📝 메시지 저장 이벤트 수신: roomId={}, sender={}, content={}",
                event.getRoomId(), event.getSenderId(), event.getContent());

            ChatRoom room = chatRoomRepository.findById(event.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

            User sender = userRepository.findById(event.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found"));

            LocalDateTime receivedTime = Instant.ofEpochMilli(event.getReceivedAt())
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime();
                
            ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .message(event.getContent())
                .receivedAt(receivedTime)
                .build();

            chatMessageRepository.save(message);

            chatMessageRepository.save(message);

            ack.acknowledge();
            log.info("✅ DB 저장 완료: messageId={}", message.getId());

        } catch (Exception e) {
            log.error("❌ 메시지 저장 실패", e);
            ack.acknowledge(); // 실패해도 오프셋 commit (중복처리 방지)
        }
    }
}
