package org.hswebframework.web.concurrent.lock.redis;

import org.hswebframework.web.concurrent.lock.AbstractLockManager;
import org.redisson.api.RedissonClient;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * @author zhouhao
 */
public class RedissonLockManager extends AbstractLockManager {
    private RedissonClient redisson;

    public RedissonLockManager(RedissonClient redisson) {
        if (null == redisson) {
            throw new NullPointerException();
        }
        this.redisson = redisson;
    }

    @Override
    protected Lock createLock(String lockName) {
        return redisson.getFairLock(lockName);
    }

    @Override
    protected ReadWriteLock createReadWriteLock(String lockName) {
        return redisson.getReadWriteLock(lockName);
    }
}
