package org.example.controller;

import com.google.common.util.concurrent.RateLimiter;
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
@RestController
@RequestMapping("/pong")
public class PongController {

    // 每秒处理1次请求
    private final RateLimiter rateLimiter = RateLimiter.create(1.0);

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

    //private final AtomicInteger counter = new AtomicInteger(0);

    //@GetMapping
    //public Mono<ResponseEntity<String>> pong() {
    //    Mono<ResponseEntity<String>> mono = Mono.just(counter.incrementAndGet())
    //            .flatMap(count -> {
    //                if (count <= 1) {
    //                    return Mono.just(ResponseEntity.ok("Hello World"));
    //                } else {
    //                    return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("429 - Too Many Requests"));
    //                }
    //            })
    //            .delayElement(Duration.ofSeconds(4))
    //            .doFinally(signal -> counter.decrementAndGet())
    //            .doOnNext(responseEntity -> {
    //                log.info("pong result: {}", JSONUtil.toJsonStr(responseEntity));
    //            });;
    //    return mono;
    //}
}
