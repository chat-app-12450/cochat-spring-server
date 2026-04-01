package com.sns.project.chat.websocket;

import com.sns.project.chat.kafka.producer.ChatEnterProducer;
import com.sns.project.chat.service.ChatRealtimeStateService;
import com.sns.project.chat.service.ChatRoomService;
import com.sns.project.core.exception.unauthorized.UnauthorizedException;
import com.sns.project.core.kafka.dto.request.KafkaChatEnterRequest;
import java.security.Principal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    // 구독은 방 단위 topic만 허용한다. roomId는 destination 경로에서만 읽는다.
    private static final Pattern SUBSCRIBE_ROOM_PATTERN = Pattern.compile("^/topic/chat/rooms/(\\d+)$");
    // 메시지 전송도 허용한 app destination만 받는다.
    private static final Pattern SEND_ROOM_PATTERN = Pattern.compile("^/app/chat/rooms/(\\d+)/messages$");
    private static final String SUBSCRIBED_ROOM_ID = "subscribedRoomId";

    private final ChatRoomService chatRoomService;
    private final ChatRealtimeStateService chatRealtimeStateService;
    private final ChatEnterProducer chatEnterProducer;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        // HTTP handshake가 끝난 뒤 들어오는 STOMP frame을 command별로 검사한다.
        // CONNECT는 "누구인지", SUBSCRIBE/SEND는 "이 방 권한이 있는지"를 확인하는 단계다.
        return switch (accessor.getCommand()) {
            case CONNECT -> handleConnect(message, accessor);
            case SUBSCRIBE -> handleSubscribe(message, accessor);
            case SEND -> handleSend(message, accessor);
            case DISCONNECT -> handleDisconnect(message, accessor);
            default -> message;
        };
    }

    private Message<?> handleConnect(Message<?> message, StompHeaderAccessor accessor) {
        // handshake 단계에서 AuthHandshakeInterceptor가 세션 attribute에 넣어둔 userId를
        // STOMP Principal로 승격한다. 이후 @MessageMapping에서는 Principal만 읽으면 된다.
        Long userId = requireHandshakeUserId(accessor);
        accessor.setUser(new StompPrincipal(userId));
        return message;
    }

    private Message<?> handleSubscribe(Message<?> message, StompHeaderAccessor accessor) {
        // 이 사용자가 해당 방의 실시간 메시지를 구독할 권한이 있는지 확인한다.
        Long userId = requireAuthenticatedUserId(accessor);
        Long roomId = extractRoomId(accessor.getDestination(), SUBSCRIBE_ROOM_PATTERN);
        // 접근권한 확인
        chatRoomService.requireParticipant(roomId, userId);
        Map<String, Object> attributes = sessionAttributes(accessor);
        Object previousRoomId = attributes.put(SUBSCRIBED_ROOM_ID, roomId);
        if (previousRoomId instanceof Long oldRoomId && !oldRoomId.equals(roomId)) {
            chatRealtimeStateService.leaveRoom(oldRoomId, userId);
        }
        chatRealtimeStateService.enterRoom(roomId, userId);

        // raw WebSocket 시절의 JOIN 의미를 STOMP에서는 SUBSCRIBE가 대신한다.
        chatEnterProducer.send(KafkaChatEnterRequest.builder()
            .roomId(roomId)
            .userId(userId)
            .build());
        return message;
    }

    private Message<?> handleSend(Message<?> message, StompHeaderAccessor accessor) {
        // SUBSCRIBE를 우회해서 SEND만 날리는 경우도 막기 위해 전송 직전 다시 참가자 검사를 한다.
        Long userId = requireAuthenticatedUserId(accessor);
        Long roomId = extractRoomId(accessor.getDestination(), SEND_ROOM_PATTERN);
        chatRoomService.requireParticipant(roomId, userId);
        return message;
    }

    private Message<?> handleDisconnect(Message<?> message, StompHeaderAccessor accessor) {
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes == null) {
            return message;
        }

        Object rawUserId = attributes.get("userId");
        Object rawRoomId = attributes.get(SUBSCRIBED_ROOM_ID);
        if (rawUserId instanceof Long userId && rawRoomId instanceof Long roomId) {
            chatRealtimeStateService.leaveRoom(roomId, userId);
        }
        return message;
    }

    private Long requireHandshakeUserId(StompHeaderAccessor accessor) {
        // WebSocket handshake는 HTTP 세계이고, STOMP는 그 이후 메시지 세계다.
        // 그래서 handshake에서 인증한 결과를 sessionAttributes로 이어받아 재사용한다.
        Object rawUserId = sessionAttributes(accessor).get("userId");
        if (rawUserId instanceof Long userId) {
            return userId;
        }
        throw new UnauthorizedException("인증된 WebSocket 세션이 아닙니다.");
    }

    private Long requireAuthenticatedUserId(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal instanceof StompPrincipal stompPrincipal) {
            // CONNECT를 통과한 정상 STOMP 세션이면 Principal에서 바로 userId를 꺼낸다.
            return stompPrincipal.userId();
        }

        // Principal이 아직 없으면 handshake attribute를 fallback으로 확인한다.
        return requireHandshakeUserId(accessor);
    }

    private Long extractRoomId(@Nullable String destination, Pattern pattern) {
        if (destination == null) {
            throw new UnauthorizedException("대상이 없는 STOMP 요청입니다.");
        }

        Matcher matcher = pattern.matcher(destination);
        if (!matcher.matches()) {
            // roomId를 body가 아니라 destination에서만 받기 때문에, 경로 규격이 틀리면 바로 차단한다.
            throw new UnauthorizedException("허용되지 않은 STOMP destination 입니다.");
        }
        return Long.parseLong(matcher.group(1));
    }

    private Map<String, Object> sessionAttributes(StompHeaderAccessor accessor) {
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes == null) {
            // 세션 정보가 없으면 handshake를 거친 정상 STOMP 요청이 아니라고 본다.
            throw new UnauthorizedException("WebSocket 세션 정보를 읽을 수 없습니다.");
        }
        return attributes;
    }
}
