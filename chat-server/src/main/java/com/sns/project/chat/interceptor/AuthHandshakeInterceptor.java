package com.sns.project.chat.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.sns.project.service.user.TokenService;

import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

  private final TokenService tokenService;

  @Override
  public boolean beforeHandshake(ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes) throws Exception {

    if (request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest httpReq = servletRequest.getServletRequest();
      Cookie[] cookies = httpReq.getCookies();

      if (cookies != null) {
        for (Cookie cookie : cookies) {
          if ("token".equals(cookie.getName())) {
            String token = cookie.getValue();
            log.info("📦 WebSocket token from cookie: {}", token);

            Long userId = verifyToken(token);
            if (userId != null) {
              attributes.put("userId", userId); // WebSocketSession에 전달됨
              return true; // 핸드셰이크 승인
            }
          }
        }
      }
    }

    // 인증 실패
    log.warn("❌ WebSocket 인증 실패 - 토큰 없음 또는 무효");
    return false;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Exception exception) {
    // 필요하면 로깅 가능
  }

  private Long verifyToken(String token) {
    return tokenService.validateToken(token); // 유효하면 userId 리턴
  }
}
