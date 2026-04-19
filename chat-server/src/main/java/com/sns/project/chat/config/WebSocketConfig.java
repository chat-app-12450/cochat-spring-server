package com.sns.project.chat.config;

import com.sns.project.chat.websocket.AuthHandshakeInterceptor;
import com.sns.project.chat.websocket.StompAuthChannelInterceptor;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.frontend.url}")
    private String frontendUrl;

    private final AuthHandshakeInterceptor authHandshakeInterceptor;
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 브라우저는 이 endpoint로 WebSocket handshake를 열고, 그 위에서 STOMP frame을 주고받는다.
        registry.addEndpoint("/ws/chat", "/ws/alarm")
            .addInterceptors(authHandshakeInterceptor)
            .setAllowedOriginPatterns(resolveAllowedOriginPatterns(frontendUrl).toArray(String[]::new));
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }

    private List<String> resolveAllowedOriginPatterns(String configuredOrigin) {
        if (configuredOrigin == null || configuredOrigin.isBlank() || "*".equals(configuredOrigin.trim())) {
            // 도메인 없이 ALB 기본 DNS 로 검증할 때는 요청 Origin 을 그대로 허용한다.
            return List.of("*");
        }

        List<String> allowedOriginPatterns = new ArrayList<>();
        allowedOriginPatterns.add(configuredOrigin);

        try {
            URI uri = URI.create(configuredOrigin);
            String host = uri.getHost();
            String scheme = uri.getScheme();

            if ("localhost".equals(host)) {
                allowedOriginPatterns.add(String.format("%s://localhost:*", scheme));
                allowedOriginPatterns.add(String.format("%s://127.0.0.1:*", scheme));
            } else if ("127.0.0.1".equals(host)) {
                allowedOriginPatterns.add(String.format("%s://127.0.0.1:*", scheme));
                allowedOriginPatterns.add(String.format("%s://localhost:*", scheme));
            }
        } catch (IllegalArgumentException ex) {
            log.warn("잘못된 spring.frontend.url 설정입니다: {}", configuredOrigin);
        }

        return allowedOriginPatterns;
    }
}
