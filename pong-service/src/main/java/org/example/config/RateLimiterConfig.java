package org.example.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public RateLimiter rateLimiter() {
        // 这里设置每秒允许的请求数，比如设置为1.0表示每秒最多1个请求
        return RateLimiter.create(1.0);
    }
}
