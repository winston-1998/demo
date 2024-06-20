package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;


/**
 * @author: WZJ
 * @create: 2024-06-20 00:10
 **/
@Slf4j
@RestController
@RequestMapping("/ping")
public class PingController {
    private final WebClient webClient;
    @Autowired
    public PingController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping
    public Mono<ResponseEntity<String>> sendPingRequest() {
        return webClient.get().retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    log.info("Request sent & Pong Respond.");
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                        log.warn("Request send & Pong throttled it.");
                        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Request send & Pong throttled it."));
                    }
                    log.error("WebClientResponseException: {}", ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error"));
                }).onErrorResume(Exception.class, ex -> {
                    log.error("Exception: {}", ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error"));
                });
    }
}
