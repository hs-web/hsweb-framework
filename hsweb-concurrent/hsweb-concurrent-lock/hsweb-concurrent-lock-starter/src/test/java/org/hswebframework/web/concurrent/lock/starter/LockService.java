package org.hswebframework.web.concurrent.lock.starter;

import org.hswebframework.web.concurrent.lock.annotation.Lock;
import org.hswebframework.web.concurrent.lock.annotation.ReadLock;
import org.hswebframework.web.concurrent.lock.annotation.WriteLock;
import org.springframework.stereotype.Service;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Service
public class LockService {

    private long counter = 0;

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
