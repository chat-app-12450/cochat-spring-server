// src/main/java/com/sns/project/chat_consumer/config/OpenSearchConfig.java
package com.sns.project.chat_consumer.config;

import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;

@Configuration
public class OpenSearchConfig {

    @Bean
    public RestHighLevelClient openSearchClient() {
        final String host = "localhost"; // 환경변수로 대체 가능
        final int port = 9200;
        final String username = "admin";
        final String password = "Developer@123";

        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(
                new org.apache.http.HttpHost(host, port, "https"))
            .setHttpClientConfigCallback(httpClientBuilder -> {
                try {
                    // 모든 인증서 신뢰 (테스트용)
                    javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
                    sslContext.init(null, new javax.net.ssl.TrustManager[]{
                        new javax.net.ssl.X509TrustManager() {
                            public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String string) {}
                            public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String string) {}
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
                        }
                    }, null);

                    return httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier((hostname, session) -> true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        return new RestHighLevelClient(builder);
    }
}