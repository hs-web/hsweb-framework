package org.hsweb.concurrent.lock.support.redis;

import org.hsweb.concurrent.lock.support.DefaultLockFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Created by zhouhao on 16-5-27.
 */
public class RedisLockFactory extends DefaultLockFactory {
    private RedisTemplate redisTemplate;

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ReadWriteLock createReadWriteLock(String key) {
        synchronized (READ_WRITE_LOCK_BASE) {
            ReadWriteLock readWriteLock = READ_WRITE_LOCK_BASE.get(key);
            if (readWriteLock == null) {
                readWriteLock = new RedisReadWriteLock(key, redisTemplate);
                READ_WRITE_LOCK_BASE.put(key, readWriteLock);
            }
            return readWriteLock;
        }
    }

    @Override
    public Lock createLock(String key) {
        synchronized (LOCK_BASE) {
            Lock lock = LOCK_BASE.get(key);
            if (lock == null) {
                lock = new RedisLock(key, redisTemplate);
                LOCK_BASE.put(key, lock);
            }
            return lock;
        }
    }
}
