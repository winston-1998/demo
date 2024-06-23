package org.example.controller

import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

@Subject(PingController)  // 标记 PingController 为被测试的主题
class PingControllerTest extends Specification {

    @Shared
    WebTestClient webTestClient;

    WebClient webClient = Mock()

    def setup() {
        webTestClient = WebTestClient.bindToController(new PingController(webClient)).build();
    }

    def "should return OK response when webClient succeeds"() {
        given:
        webClient.get() >> {
            WebClient.RequestHeadersUriSpec requestHeadersUriSpec = Mock()
            requestHeadersUriSpec.retrieve() >> {
                WebClient.ResponseSpec responseSpec = Mock()
                responseSpec.bodyToMono(String.class) >> Mono.just("World")
                responseSpec
            }
            requestHeadersUriSpec
        }

        expect:
        webTestClient.get().uri("/ping")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String)
                .isEqualTo("World")
    }

    def "should return TOO_MANY_REQUESTS response when webClient throws WebClientResponseException with 429"() {
        given:
        def exception = new WebClientResponseException("Too Many Requests", 429, "Too Many Requests", null, null, null)
        webClient.get() >> {
            WebClient.RequestHeadersUriSpec requestHeadersUriSpec = Mock()
            requestHeadersUriSpec.retrieve() >> {
                WebClient.ResponseSpec responseSpec = Mock()
                responseSpec.bodyToMono(String.class) >> Mono.error(exception)
                responseSpec
            }
            requestHeadersUriSpec
        }

        expect:
        webTestClient.get().uri("/ping")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
    }

    def "should return TOO_MANY_REQUESTS response when webClient throws WebClientResponseException with 502"() {
        given:
        def exception = new WebClientResponseException(HttpStatus.BAD_GATEWAY.getReasonPhrase(), HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase(), null, null, null)
        webClient.get() >> {
            WebClient.RequestHeadersUriSpec requestHeadersUriSpec = Mock()
            requestHeadersUriSpec.retrieve() >> {
                WebClient.ResponseSpec responseSpec = Mock()
                responseSpec.bodyToMono(String.class) >> Mono.error(exception)
                responseSpec
            }
            requestHeadersUriSpec
        }

        expect:
        webTestClient.get().uri("/ping")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }


    def "should return INTERNAL_SERVER_ERROR response when webClient throws Exception"() {
        given:
        def exception = new RuntimeException("Internal Server Error")
        webClient.get() >> {
            WebClient.RequestHeadersUriSpec requestHeadersUriSpec = Mock()
            requestHeadersUriSpec.retrieve() >> {
                WebClient.ResponseSpec responseSpec = Mock()
                responseSpec.bodyToMono(String.class) >> Mono.error(exception)
                responseSpec
            }
            requestHeadersUriSpec
        }


        expect:
        webTestClient.get().uri("/ping")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody(String).isEqualTo("Internal Server Error")
    }
}
