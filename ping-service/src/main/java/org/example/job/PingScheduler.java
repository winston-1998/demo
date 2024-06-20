package org.example.job;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.controller.PingController;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;


/**
 * @author: WZJ
 * @create: 2024-06-20 00:12
 **/
@Slf4j
@Component
@AllArgsConstructor
public class PingScheduler {

    private static final String LOCK_FILE_PATH1 = System.getProperty("java.io.tmpdir") + File.separator + "pong-service-lock1";
    private static final String LOCK_FILE_PATH2 = System.getProperty("java.io.tmpdir") + File.separator + "pong-service-lock2";

    private final PingController pingController;
    @Scheduled(fixedRate = 1000)
    public void run() {
        try (RandomAccessFile lockFile = new RandomAccessFile(LOCK_FILE_PATH1, "rw");
             FileChannel channel = lockFile.getChannel(); ) {
            FileLock fileLock = channel.tryLock();
            if (fileLock != null) {
                // 说明获取到了锁1
                pingController.sendPingRequest().subscribe(response -> {
                    log.info("Response: {}", JSONUtil.toJsonStr(response.getBody()) );
                });
                fileLock.release();
            }
        } catch (Exception  e) {
            // 说明没获取到了锁1, 去抢锁2
            try (RandomAccessFile lockFile = new RandomAccessFile(LOCK_FILE_PATH2, "rw");
                 FileChannel channel = lockFile.getChannel();) {
                FileLock fileLock = channel.tryLock();
                if (fileLock != null) {
                    // 说明获取到了锁2
                    pingController.sendPingRequest().subscribe(response -> {
                        log.info("Response: {}", JSONUtil.toJsonStr(response.getBody()));
                    });
                    fileLock.release();
                }
            } catch (Exception exception) {
                // 说明没获取到了锁2
                log.warn("Request send & Pong throttled it.");
            }
        }
    }
}
