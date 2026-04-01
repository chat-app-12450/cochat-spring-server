package com.sns.project.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.auth.cookie")
public class AuthCookieProperties {

    // HttpOnly 인증 쿠키와 JS가 읽을 수 있는 CSRF 쿠키를 분리 관리한다.
    private String accessTokenName = "ACCESS_TOKEN";
    private String refreshTokenName = "REFRESH_TOKEN";
    private String csrfTokenName = "XSRF-TOKEN";
    private boolean secure = false;
    private boolean httpOnly = true;
    private String sameSite = "Lax";
    private String path = "/";
    private String domain;
    private long maxAgeSeconds = 3600;
    private long refreshMaxAgeSeconds = 604800;
}
