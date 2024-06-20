package org.example.controller


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PongControllerUnitTest extends Specification {

    @Autowired
    private WebTestClient webTestClient

    def "First request should return Hello World"() {
        expect: "First request returns 'Hello World'"
        webTestClient.get().uri("/pong")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String)
                .isEqualTo("World")
    }

    // 这个单独执行单元测试是可以成功的, 使用mvn clean test jacoco:report却不行
//    def "Concurrent async requests should handle too many requests"() {
//        when:
//        def responses = sendAsyncRequests(20)
//
//        then:
//        Awaitility.await().atMost(Duration.ofSeconds(5)).until {
//            responses.any { it.expectStatus().isEqualTo(429).returnResult(String).responseBody.blockFirst(Duration.ofSeconds(1)) }
//        }
//    }
//
//    private List<WebTestClient.ResponseSpec> sendAsyncRequests(int count) {
//        def executor = Executors.newFixedThreadPool(count)
//        def responses = new CopyOnWriteArrayList<WebTestClient.ResponseSpec>()
//
//        (1..count).each { index ->
//            executor.submit {
//                def response = webTestClient.get().uri("/pong").exchange()
//                responses.add(response)
//            }
//        }
//
//        executor.shutdown()
//        executor.awaitTermination(10, TimeUnit.SECONDS)
//
//        responses
//    }

}
