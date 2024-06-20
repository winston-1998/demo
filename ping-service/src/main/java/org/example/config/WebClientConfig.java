package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author: WZJ
 * @create: 2024-06-20 22:10
 **/
@Configuration
public class WebClientConfig {

    @Value("${pong.server.url:http://localhost:8081/pong}")
    private String requestUrl;

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl(requestUrl).build();
    }
}