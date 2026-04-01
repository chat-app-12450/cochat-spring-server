package com.sns.project.chat.controller;

import com.sns.project.chat.controller.dto.request.StompChatSendRequest;
import com.sns.project.chat.kafka.producer.MessageBroadcastProducer;
import com.sns.project.chat.kafka.producer.MessageVectorProducer;
import com.sns.project.chat.service.ChatService;
import com.sns.project.chat.websocket.StompPrincipal;
import com.sns.project.core.domain.chat.ChatMessage;
import com.sns.project.core.exception.unauthorized.UnauthorizedException;
import com.sns.project.core.kafka.dto.request.KafkaNewMsgRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Controller
@Validated
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatService chatService;
    private final MessageVectorProducer messageVectorProducer;
    private final MessageBroadcastProducer messageBroadcastProducer;

    @MessageMapping("/chat/rooms/{roomId}/messages")
    public void sendMessage(@DestinationVariable Long roomId,
                            @Valid StompChatSendRequest payload,
                            Principal principal) {
        Long senderId = extractUserId(principal);
        ChatMessage savedMessage = chatService.saveMessage(roomId, senderId, payload.getMessage());

        KafkaNewMsgRequest kafkaNewMsgRequest = KafkaNewMsgRequest.builder()
            .roomId(roomId)
            .senderId(senderId)
            .content(payload.getMessage())
            .receivedAt(savedMessage.getReceivedAt().toEpochSecond(ZoneOffset.UTC))
            .messageId(savedMessage.getId())
            .build();

        log.info("STOMP message accepted: roomId={}, senderId={}", roomId, senderId);
        messageVectorProducer.send(kafkaNewMsgRequest);
        messageBroadcastProducer.sendDeliver(kafkaNewMsgRequest);
    }

    private Long extractUserId(Principal principal) {
        if (principal instanceof StompPrincipal stompPrincipal) {
            return stompPrincipal.userId();
        }
        throw new UnauthorizedException("인증된 STOMP 사용자 정보가 없습니다.");
    }
}
