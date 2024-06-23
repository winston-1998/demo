package org.example.controller;

import com.google.common.util.concurrent.RateLimiter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * @author: WZJ
 * @create: 2024-06-21 02:45
 **/
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/pong")
public class PongController {

    private final RateLimiter rateLimiter;

    @GetMapping
    public Mono<String> pong() {
        if (shouldThrottle()) {
            return Mono.error(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests"));
        }
        return Mono.just("World");
    }

    public boolean shouldThrottle() {
        return !rateLimiter.tryAcquire();
    }
}
