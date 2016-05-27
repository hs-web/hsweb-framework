package org.hsweb.concurrent.lock.support.redis;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Created by zhouhao on 16-5-27.
 */
public class RedisReadWriteLock implements ReadWriteLock {
    static final String PREFIX = "lock:";
    static final byte[] LOCK_VALUE = new byte[0];

    private ReadLock readLock;

    private WriteLock writeLock;

    private String key;

    private RedisTemplate redisTemplate;

    public RedisReadWriteLock(String key, RedisTemplate redisTemplate) {
        Assert.notNull(key);
        Assert.notNull(redisTemplate);
        this.key = key;
        this.redisTemplate = redisTemplate;
        readLock = new ReadLock();
        writeLock = new WriteLock();
    }

    @Override
    public Lock readLock() {
        return readLock;
    }

    @Override
    public Lock writeLock() {
        return writeLock;
    }

    private byte[] getReadKey() {
        return (PREFIX + key + ".read.lock").getBytes();
    }

    private byte[] getWriteKey() {
        return (PREFIX + key + ".write.lock").getBytes();
    }

    protected void sleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
    }

    class ReadLock implements Lock {
        @Override
        public void lock() {
            redisTemplate.execute((RedisCallback<String>) connection -> {
                boolean locked = false;
                do {
                    locked = connection.exists(getWriteKey());
                    if (!locked) {
                        connection.setNX(getReadKey(), LOCK_VALUE);
                        locked = true;
                    }
                    sleep();
                } while (!locked);
                return null;
            });
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            boolean locked = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection ->
            {
                boolean writeLocked = connection.exists(getWriteKey());
                if (!writeLocked) {
                    connection.setNX(getReadKey(), LOCK_VALUE);
                    writeLocked = true;
                }
                return writeLocked;
            });
            if (!locked) throw new InterruptedException("");
        }

        @Override
        public boolean tryLock() {
            return (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection ->
                            connection.setNX(getReadKey(), LOCK_VALUE)
            );
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            byte[] error = new byte[1];
            boolean success = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                boolean locked = false;
                long startWith = System.nanoTime();
                do {
                    locked = connection.exists(getWriteKey());
                    if (!locked) {
                        connection.setNX(getReadKey(), LOCK_VALUE);
                        connection.expire(getReadKey(), 30);
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
            redisTemplate.execute((RedisCallback) conn -> conn.del(getReadKey()));
        }

        @Override
        public Condition newCondition() {
            return null;
        }
    }

    class WriteLock implements Lock {
        @Override
        public void lock() {
            redisTemplate.execute((RedisCallback<String>) connection -> {
                boolean locked = false, readLocked = false;
                do {
                    readLocked = connection.exists(getReadKey());
                    if (!readLocked) {
                        locked = connection.setNX(getWriteKey(), LOCK_VALUE);
                    }
                    sleep();
                } while (!locked);
                return null;
            });
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            boolean locked = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection ->
            {
                boolean readLocked = connection.exists(getReadKey());
                if (!readLocked) {
                    return connection.setNX(getWriteKey(), LOCK_VALUE);
                }
                return false;
            });
            if (!locked) throw new InterruptedException("");
        }

        @Override
        public boolean tryLock() {
            return (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                if (connection.exists(getReadKey())) return false;
                return connection.setNX(getWriteKey(), LOCK_VALUE);
            });
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            byte[] error = new byte[1];
            boolean success = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                boolean locked = false;
                long startWith = System.nanoTime();
                do {
                    locked = connection.exists(getReadKey());
                    if (!locked) {
                        locked = connection.setNX(getWriteKey(), LOCK_VALUE);
                        if (locked) {
                            connection.expire(getWriteKey(), 30);
                            return true;
                        }
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
            redisTemplate.execute((RedisCallback) conn -> conn.del(getWriteKey()));
        }

        @Override
        public Condition newCondition() {
            return null;
        }
    }
}
