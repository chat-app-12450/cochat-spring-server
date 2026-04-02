package com.sns.project.chat.controller;

import com.sns.project.chat.controller.dto.request.StompChatSendRequest;
import com.sns.project.chat.service.ChatService;
import com.sns.project.chat.websocket.StompPrincipal;
import com.sns.project.core.domain.chat.ChatMessage;
import com.sns.project.core.exception.unauthorized.UnauthorizedException;
import jakarta.validation.Valid;
import java.security.Principal;
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

    @MessageMapping("/chat/rooms/{roomId}/messages")
    public void sendMessage(@DestinationVariable Long roomId,
                            @Valid StompChatSendRequest payload,
                            Principal principal) {
        Long senderId = extractUserId(principal);
        ChatMessage savedMessage = chatService.saveMessage(roomId, senderId, payload.getMessage());

        log.info("STOMP message accepted: roomId={}, senderId={}", roomId, senderId);
    }

    private Long extractUserId(Principal principal) {
        if (principal instanceof StompPrincipal stompPrincipal) {
            return stompPrincipal.userId();
        }
        throw new UnauthorizedException("인증된 STOMP 사용자 정보가 없습니다.");
    }
}
