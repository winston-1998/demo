package org.example.controller

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Subject

class PingControllerTest extends Specification {

    @Subject
    PingController pingController

    WebClient webClient = Mock()

    def setup() {
        pingController = new PingController(webClient)
    }

    def "should return OK response when webClient succeeds"() {
        given:
        def response = "Pong"
        webClient.get() >> {
            WebClient.RequestHeadersUriSpec requestHeadersUriSpec = Mock()
            requestHeadersUriSpec.retrieve() >> {
                WebClient.ResponseSpec responseSpec = Mock()
                responseSpec.bodyToMono(String.class) >> Mono.just(response)
                responseSpec
            }
            requestHeadersUriSpec
        }

        when:
        def result = pingController.sendPingRequest().block()

        then:
        result.getStatusCode() == HttpStatus.OK
        result.getBody() == response
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

        when:
        def result = pingController.sendPingRequest().block()

        then:
        result.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS
        result.getBody() == "Request send & Pong throttled it."
    }

    def "should return INTERNAL_SERVER_ERROR response when webClient throws general exception"() {
        given:
        def exception = new RuntimeException("General Error")
        webClient.get() >> {
            WebClient.RequestHeadersUriSpec requestHeadersUriSpec = Mock()
            requestHeadersUriSpec.retrieve() >> {
                WebClient.ResponseSpec responseSpec = Mock()
                responseSpec.bodyToMono(String.class) >> Mono.error(exception)
                responseSpec
            }
            requestHeadersUriSpec
        }

        when:
        def result = pingController.sendPingRequest().block()

        then:
        result.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR
        result.getBody() == "Internal Server Error"
    }
}
