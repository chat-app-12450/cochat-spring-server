package com.sns.project.chat.interceptor;

import com.sns.project.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.sns.project.auth.TokenService;

import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

  private final TokenService tokenService;
  private final AuthCookieService authCookieService;

  @Override
  public boolean beforeHandshake(ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes) throws Exception {

    try {
      if (request instanceof ServletServerHttpRequest servletRequest) {
        HttpServletRequest httpReq = servletRequest.getServletRequest();
        String token = authCookieService.extractAccessToken(httpReq).orElse(null);
        if (token != null) {
          Long userId = tokenService.validateToken(token);
          if (userId != null) {
            attributes.put("userId", userId); // STOMP CONNECT 단계에서 다시 Principal로 승격된다.
            return true; // 핸드셰이크 승인
          }
        }
      }
    } catch (RuntimeException ignored) {
      // 인증 실패는 아래에서 공통 401 처리한다.
    }

    // 인증 실패 시에도 토큰/쿠키 값은 로그에 남기지 않는다.
    log.warn("WebSocket authentication failed");
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    return false;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Exception exception) {
    // 필요하면 로깅 가능
  }

 
}
