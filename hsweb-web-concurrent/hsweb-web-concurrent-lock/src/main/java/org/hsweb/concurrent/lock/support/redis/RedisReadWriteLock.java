package org.hsweb.concurrent.lock.support.redis;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Created by zhouhao on 16-5-27.
 */
public class RedisReadWriteLock implements ReadWriteLock {
    static final String PREFIX = "lock:";
    static final long DEFAULT_EXPIRE = 60;
    private ReadLock readLock;
    private WriteLock writeLock;
    private String key;
    private long lockKeyExpireTime = DEFAULT_EXPIRE;
    private long waitTime = 30;
    protected byte[] lockValue;
    private byte[] readLockKey, writeLockKey;

    private RedisTemplate redisTemplate;

    public RedisReadWriteLock(String key, RedisTemplate redisTemplate) {
        Assert.notNull(key);
        Assert.notNull(redisTemplate);
        this.key = key;
        this.redisTemplate = redisTemplate;
        readLock = new ReadLock();
        writeLock = new WriteLock();
        readLockKey = (PREFIX + key + ".read.lock").getBytes();
        writeLockKey = (PREFIX + key + ".write.lock").getBytes();
        lockValue = (UUID.randomUUID().toString()).getBytes();
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
        return readLockKey;
    }

    private byte[] getWriteKey() {
        return writeLockKey;
    }

    protected void sleep() {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
        }
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    public void setLockKeyExpireTime(long lockKeyExpireTime) {
        this.lockKeyExpireTime = lockKeyExpireTime;
    }

    class ReadLock implements Lock {
        public byte[] lockValue() {
            return new String(lockValue).concat(Thread.currentThread().getId() + "").getBytes();
        }

        @Override
        public void lock() {
            redisTemplate.execute((RedisCallback<String>) connection -> {
                boolean locked = false;
                do {
                    if (!connection.exists(getWriteKey())) {
                        connection.setNX(getReadKey(), lockValue());
                        connection.expire(getReadKey(), lockKeyExpireTime);
                        locked = true;
                    } else
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
                    if (connection.setNX(getReadKey(), lockValue)) {
                        connection.expire(getReadKey(), lockKeyExpireTime);
                    }
                    writeLocked = true;
                }
                return writeLocked;
            });
            if (!locked) throw new InterruptedException("");
        }

        @Override
        public boolean tryLock() {
            return (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection ->
            {
                if (connection.setNX(getReadKey(), lockValue)) {
                    connection.expire(getReadKey(), lockKeyExpireTime);
                }
                return false;
            });
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            byte[] error = new byte[1];
            boolean success = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                boolean locked = false;
                long startWith = System.nanoTime();
                do {
                    if (!connection.exists(getWriteKey())) {
                        connection.setNX(getReadKey(), lockValue);
                        connection.expire(getReadKey(), lockKeyExpireTime);
                        return true;
                    }
                    long now = System.nanoTime();
                    if (now - startWith > unit.toNanos(time)) {
                        error[0] = 1;
                        return false;
                    }
                    sleep();
                } while (!locked);
                return false;
            });
            if (error[0] == 1) {
                throw new InterruptedException("try lock time out!");
            }
            return success;
        }

        @Override
        public void unlock() {
            redisTemplate.execute((RedisCallback) conn -> {
                byte[] lock = conn.get(getReadKey());
                if (lock == null) return null;
                //当前读锁为自己持有 才解锁
                if (new String(lock).equals(new String(lockValue()))) {
                    conn.del(getReadKey());
                }
                return null;
            });
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
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
                        locked = connection.setNX(getWriteKey(), lockValue);
                        connection.expire(getWriteKey(), lockKeyExpireTime);
                    } else
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
                    boolean _locked = connection.setNX(getWriteKey(), lockValue);
                    if (_locked) connection.expire(getWriteKey(), lockKeyExpireTime);
                    return _locked;
                }
                return false;
            });
            if (!locked) throw new InterruptedException("");
        }

        @Override
        public boolean tryLock() {
            return (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                if (connection.exists(getReadKey())) return false;
                boolean locked = connection.setNX(getWriteKey(), lockValue);
                if (locked)
                    connection.expire(getWriteKey(), lockKeyExpireTime);
                return locked;
            });
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            byte[] error = new byte[1];
            boolean success = (Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                boolean locked = false;
                long startWith = System.nanoTime();
                do {
                    if (!connection.exists(getReadKey())) {
                        locked = connection.setNX(getWriteKey(), lockValue);
                        if (locked) {
                            connection.expire(getWriteKey(), lockKeyExpireTime);
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
            throw new UnsupportedOperationException();
        }
    }
}
