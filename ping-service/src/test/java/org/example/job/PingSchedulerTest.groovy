package org.example.job

import org.example.controller.PingController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Subject

import java.nio.channels.FileChannel
import java.nio.channels.FileLock

class PingSchedulerTest extends Specification {

    @Subject
    PingScheduler pingScheduler

    PingController pingController = Mock()
    FileChannel fileChannel1 = Mock()
    FileChannel fileChannel2 = Mock()
    FileLock fileLock1 = Mock()
    FileLock fileLock2 = Mock()

    def setup() {
        pingScheduler = new PingScheduler(pingController)
    }

    def "should send ping request and release lock when lock1 is available"() {
        given:
        RandomAccessFile lockFile1 = Mock()
        lockFile1.getChannel() >> fileChannel1
        fileChannel1.tryLock() >> fileLock1

        GroovyMock(RandomAccessFile, global: true)
        new RandomAccessFile(PingScheduler.LOCK_FILE_PATH1, "rw") >> lockFile1

        when:
        pingScheduler.run()

        then:
        1 * pingController.sendPingRequest() >> Mono.just(new ResponseEntity<>("Pong", HttpStatus.OK))
        1 * fileLock1.release()
    }

    def "should send ping request and release lock when lock1 is not available but lock2 is"() {
        given:
        RandomAccessFile lockFile1 = Mock()
        lockFile1.getChannel() >> fileChannel1
        RandomAccessFile lockFile2 = Mock()
        lockFile2.getChannel() >> fileChannel2

        fileChannel1.tryLock() >> null
        fileChannel2.tryLock() >> fileLock2

        GroovyMock(RandomAccessFile, global: true)
        new RandomAccessFile(PingScheduler.LOCK_FILE_PATH1, "rw") >> lockFile1
        new RandomAccessFile(PingScheduler.LOCK_FILE_PATH2, "rw") >> lockFile2

        when:
        pingScheduler.run()

        then:
        1 * pingController.sendPingRequest() >> Mono.just(new ResponseEntity<>("Pong", HttpStatus.OK))
        1 * fileLock2.release()
    }

    def "should log warning when both locks are not available"() {
        given:
        RandomAccessFile lockFile1 = Mock()
        lockFile1.getChannel() >> fileChannel1
        RandomAccessFile lockFile2 = Mock()
        lockFile2.getChannel() >> fileChannel2

        fileChannel1.tryLock() >> null
        fileChannel2.tryLock() >> null

        GroovyMock(RandomAccessFile, global: true)
        new RandomAccessFile(PingScheduler.LOCK_FILE_PATH1, "rw") >> lockFile1
        new RandomAccessFile(PingScheduler.LOCK_FILE_PATH2, "rw") >> lockFile2

        when:
        pingScheduler.run()

        then:
        0 * pingController.sendPingRequest()
        1 * _ >> { log.warn("Request send & Pong throttled it.") }
    }
}
