package com.sns.project.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class CustomCorsFilter {
  @Value("${spring.frontend.url}")
  private String frontEndUrl;

  @Bean
  public FilterRegistrationBean<CorsFilter> corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(resolveAllowedOriginPatterns(frontEndUrl));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Content-Type", "X-CSRF-Token", "Accept"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // HTTP CORS 정책은 여기 한 곳에서만 관리한다. WebSocket origin은 WebSocketConfig에서 별도 관리한다.
    source.registerCorsConfiguration("/api/**", config);

    FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>(new CorsFilter(source));
    // CORS 헤더는 다른 필터가 401/403을 내려도 항상 먼저 붙어야 브라우저가 정상 응답으로 해석한다.
    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registrationBean;
  }

  private List<String> resolveAllowedOriginPatterns(String configuredOrigin) {
    List<String> allowedOriginPatterns = new ArrayList<>();
    allowedOriginPatterns.add(configuredOrigin);

    try {
      URI uri = URI.create(configuredOrigin);
      String host = uri.getHost();
      String scheme = uri.getScheme();

      // 개발 환경에서는 localhost/127.0.0.1를 번갈아 열거나 포트를 바꿔도 같은 로컬 프론트로 본다.
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
