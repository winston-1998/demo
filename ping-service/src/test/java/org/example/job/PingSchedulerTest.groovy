package org.example.job

import org.example.controller.PingController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import spock.lang.Specification

class PingSchedulerTest extends Specification {

    PingController pingController = Mock(PingController.class)

    PingScheduler pingScheduler

    void setup() {
        pingScheduler = new PingScheduler(pingController)
    }

    def "Three settings for concurrent requests. If the method is called twice, it means there is no problem."() {
        given:
        def mockResponse = Mock(Mono<ResponseEntity<String>>)

        when:
        def threads = (1..3).collect {
            new Thread({
                pingScheduler.run()
            })
        }
        threads*.start()
        threads*.join()

        then:
        2 * pingController.sendRequestToPongService() >> { mockResponse }

        and:
        noExceptionThrown()
    }

    def "Set three requests and execute them once per second. If the method is called three times, it means there is no problem."() {
        given:
        def mockResponse = Mock(Mono<ResponseEntity<String>>)

        when:
        3.times {
            pingScheduler.run()
            Thread.sleep(1000)
        }

        then:
        3 * pingController.sendRequestToPongService() >> { mockResponse }

        and:
        noExceptionThrown()
    }

}
