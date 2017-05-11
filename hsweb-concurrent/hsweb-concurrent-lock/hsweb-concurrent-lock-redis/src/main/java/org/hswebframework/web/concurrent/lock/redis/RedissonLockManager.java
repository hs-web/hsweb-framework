package org.hswebframework.web.concurrent.lock.redis;

import org.hswebframework.web.concurrent.lock.AbstactLocakManager;
import org.redisson.Redisson;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 *
 * @author zhouhao
 */
public class RedissonLockManager extends AbstactLocakManager {
    private Redisson redisson;

    public RedissonLockManager(Redisson redisson) {
        if (null == redisson) throw new NullPointerException();
        this.redisson = redisson;
    }

    @Override
    protected Lock createLock(String lockKey) {
        return redisson.getLock(lockKey);
    }

    @Override
    protected ReadWriteLock createReadWriteLock(String lockKey) {
        return redisson.getReadWriteLock(lockKey);
    }
}
