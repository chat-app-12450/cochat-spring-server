package com.sns.project.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@Slf4j
@Component
public class CustomCorsFilter {
 @Value("${spring.frontend.url}")
 private String FRONT_END_URL;
  @Bean
  public CorsFilter corsFilter() {
    log.info("corsfilter: {}", FRONT_END_URL);
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(FRONT_END_URL));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return new CorsFilter(source);
  }
}
