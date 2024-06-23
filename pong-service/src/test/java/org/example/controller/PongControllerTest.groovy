package org.example.controller

import com.google.common.util.concurrent.RateLimiter
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title

@Title("PongController Unit Tests")
class PongControllerTest extends Specification {

    @Shared // 用于在测试类中共享变量
    WebTestClient webTestClient

    RateLimiter rateLimiter = Mock(RateLimiter.class)

    def setup() {
        webTestClient = WebTestClient.bindToController(new PongController(rateLimiter)).build()
    }


    def "should return 'World' when rate limiter allows request"() {
        given:
        rateLimiter.tryAcquire() >> true

        expect:
        webTestClient.get().uri("/pong")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String)
            .isEqualTo("World")
    }

    def "should return 429 Too Many Requests when rate limiter blocks request"() {
        given:
        rateLimiter.tryAcquire() >> false

        expect:
        webTestClient.get().uri("/pong")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
    }
}
