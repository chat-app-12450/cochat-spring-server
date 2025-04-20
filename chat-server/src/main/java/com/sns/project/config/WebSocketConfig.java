package com.sns.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import lombok.RequiredArgsConstructor;

import com.sns.project.chat.handler.ChatWebSocketHandler;
import com.sns.project.chat.interceptor.AuthHandshakeInterceptor;

@Configuration
@EnableWebSocket  // ✅ Use WebSocket instead of STOMP
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final AuthHandshakeInterceptor authHandshakeInterceptor;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOrigins("*");

        registry.addHandler(chatWebSocketHandler, "/ws/alarm")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
