package org.hsweb.concurrent.lock.support.redis;

import org.hsweb.concurrent.lock.exception.LockException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by zhouhao on 16-5-27.
 */
public class RedisLock implements Lock {

    static final String PREFIX = "lock:";
    static final byte[] LOCK_VALUE = new byte[0];

    private RedisTemplate redisTemplate;
    private String key;

    private byte[] getKey() {
        return (PREFIX + key + ".lock").getBytes();
    }

    public RedisLock(String key, RedisTemplate redisTemplate) {
        Assert.notNull(key);
        Assert.notNull(redisTemplate);
        this.key = key;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void lock() {
        redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            boolean locked = false;
            do {
                locked = connection.setNX(getKey(), LOCK_VALUE);
                sleep();
            } while (!locked);
            return true;
        });
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        boolean locked = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection ->
                        connection.setNX(getKey(), LOCK_VALUE)
        );
        if (!locked) throw new InterruptedException(new String(getKey()) + " is locked!");
    }

    @Override
    public boolean tryLock() {
        return (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection ->
                        connection.setNX(getKey(), LOCK_VALUE)
        );
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        byte[] error = new byte[1];
        boolean success = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            boolean locked = false;
            long startWith = System.nanoTime();
            do {
                locked = connection.setNX(getKey(), LOCK_VALUE);
                if (locked) {
                    connection.expire(getKey(), 30);
                    return true;
                }
                long now = System.nanoTime();
                if (now - startWith > unit.toNanos(time)) {
                    error[0] = 1;
                    return false;
                }
                sleep();
            } while (!locked);
            return null;
        });
        if (error[0] == 1) {
            throw new InterruptedException("lock time out!");
        }
        return success;
    }

    @Override
    public void unlock() {
        redisTemplate.execute((RedisCallback) conn -> conn.del(getKey()));
    }

    @Override
    public Condition newCondition() {
        throw new LockException("method not support");
    }

    protected void sleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
    }
}
