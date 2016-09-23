package org.hsweb.concurrent.lock.support.redis;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.Assert;

import java.util.*;
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
    private long lockKeyExpireTime = DEFAULT_EXPIRE;
    private long waitTime = 30;
    protected String lockValue;
    private String readLockKey, writeLockKey;

    private static DefaultRedisScript<Boolean> redisScriptRead;
    private static DefaultRedisScript<Boolean> redisScriptWrite;

    static {
        //初始化脚本
        redisScriptRead = new DefaultRedisScript<>();
        redisScriptWrite = new DefaultRedisScript<>();

        redisScriptRead.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/vcheckAndsAdd.lua", RedisReadWriteLock.class)));
        redisScriptRead.setResultType(Boolean.class);
        redisScriptWrite.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/scheckAndVset.lua", RedisReadWriteLock.class)));
        redisScriptWrite.setResultType(Boolean.class);
    }

    private StringRedisTemplate redisTemplate;

    public RedisReadWriteLock(String key, RedisTemplate redisTemplate) {
        Assert.notNull(key);
        Assert.notNull(redisTemplate);
        this.redisTemplate = new StringRedisTemplate(redisTemplate.getConnectionFactory());
        readLockKey = PREFIX + key + ".read.lock";
        writeLockKey = PREFIX + key + ".write.lock";
        readLock = new ReadLock();
        writeLock = new WriteLock();
        lockValue = UUID.randomUUID().toString();
    }

    @Override
    public Lock readLock() {
        return readLock;
    }

    @Override
    public Lock writeLock() {
        return writeLock;
    }

    private String getReadKey() {
        return readLockKey;
    }

    private String getWriteKey() {
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

        private List<String> keys = new ArrayList<>();

        public ReadLock() {
            super();
            keys.add(getWriteKey().toString());
            keys.add(getReadKey().toString());
        }

        public String lockValue() {
            return new String(lockValue).concat(Thread.currentThread().getId() + "");
        }

        @Override
        public void lock() {

            while (true) {
                Boolean locked = redisTemplate.execute(redisScriptRead, keys, lockValue());
                if (!locked) {
                    sleep();
                } else {
                    /*
                    * 此处增加对所有读锁的过期
                    * 1、防止项目停止，导致读锁一直存在
                    *
                    * @TODO 后期可以抽出到 redisScriptRead脚本中
                    * */
                    expire();
                    break;
                }
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {

            boolean locked = redisTemplate.execute(redisScriptRead, keys, lockValue());
            if (locked) {
                expire();
            } else {
                throw new InterruptedException("could not get the read lock!");
            }
        }

        @Override
        public boolean tryLock() {
            boolean locked = redisTemplate.execute(redisScriptRead, keys, lockValue());
            if (locked) {
                expire();
            }
            return locked;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            byte[] error = new byte[1];

            boolean locked;
            long startWith = System.nanoTime();
            do {

                locked = redisTemplate.execute(redisScriptRead, keys, lockValue());
                if (locked) {
                    expire();
                    break;
                }

                long now = System.nanoTime();
                if (now - startWith > unit.toNanos(time)) {
                    error[0] = 1;
                    break;
                }
                sleep();
            } while (!locked);

            if (error[0] == 1) {
                throw new InterruptedException("try lock time out!");
            }
            return locked;
        }

        @Override
        public void unlock() {
            redisTemplate.execute((RedisCallback) conn -> {
                StringRedisConnection strConn = (StringRedisConnection) conn;
                Set<String> locks = strConn.sMembers(getReadKey());

                if (locks == null || locks.size() == 0)
                    return null;
                //当前读锁为自己持有 才解锁
                if (locks.contains(lockValue())) {
                    strConn.sRem(getReadKey(), lockValue());
                }
                return null;
            });
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        private void expire() {
            redisTemplate.expire(getReadKey(), lockKeyExpireTime, TimeUnit.SECONDS);
        }
    }

    class WriteLock implements Lock {

        private List<String> keys = new ArrayList<>();

        public WriteLock() {
            super();
            keys.add(getReadKey());
            keys.add(getWriteKey());
        }

        @Override
        public void lock() {

            boolean locked;

            do {
                locked = redisTemplate.execute(redisScriptWrite, keys, lockValue);
                if (locked) {
                    expire();
                } else {
                    sleep();
                }
            } while (!locked);
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            boolean locked = redisTemplate.execute(redisScriptWrite, keys, lockValue);
            if (locked) {
                expire();
            } else {
                throw new InterruptedException("");
            }
        }

        @Override
        public boolean tryLock() {
            boolean locked = redisTemplate.execute(redisScriptWrite, keys, lockValue);
            if (locked) {
                expire();
            }
            return locked;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            byte[] error = new byte[1];

            boolean locked;
            long startWith = System.nanoTime();
            do {
                locked = redisTemplate.execute(redisScriptWrite, keys, lockValue);
                long now = System.nanoTime();
                if (now - startWith > unit.toNanos(time)) {
                    error[0] = 1;
                    break;
                }
                sleep();
            } while (!locked);

            if (locked) {
                expire();
            }

            if (error[0] == 1) {
                throw new InterruptedException("lock time out!");
            }
            return locked;
        }

        @Override
        public void unlock() {
            redisTemplate.execute((RedisCallback) conn -> {
                StringRedisConnection strConn = (StringRedisConnection) conn;

                strConn.del(getWriteKey());
                return null;
            });
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        private void expire() {
            redisTemplate.expire(getWriteKey(), lockKeyExpireTime, TimeUnit.SECONDS);
        }
    }
}
