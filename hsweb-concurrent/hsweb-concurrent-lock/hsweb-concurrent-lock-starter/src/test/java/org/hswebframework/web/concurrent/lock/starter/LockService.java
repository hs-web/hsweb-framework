package org.hswebframework.web.concurrent.lock.starter;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.concurrent.lock.annotation.Lock;
import org.hswebframework.web.concurrent.lock.annotation.ReadLock;
import org.hswebframework.web.concurrent.lock.annotation.WriteLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author zhouhao
 */
@Slf4j
public class LockService {

    private long counter = 0;

//    @Scheduled(cron = "0/1 * * * * ?")
//    @Lock("test2")
//    public void test() throws InterruptedException {
//        log.info("try lock");
//        Thread.sleep(5000);
//        log.info("un lock");
//    }

    @Lock("lock_${#key}")
    public long testLockSleep(String key, long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return counter;
    }

    @Lock("lock_${#key}")
    public long testLock(String key) {
        return counter++;
    }

    @ReadLock("lock_${#key}")
    public long testReadLock(String key) {
        return counter++;
    }

    @WriteLock("lock_${#key}")
    public long testWriteLock(String key) {
        return counter++;
    }

    public long getCounter() {
        return counter;
    }

    public void reset() {
        counter = 0;
    }
}
