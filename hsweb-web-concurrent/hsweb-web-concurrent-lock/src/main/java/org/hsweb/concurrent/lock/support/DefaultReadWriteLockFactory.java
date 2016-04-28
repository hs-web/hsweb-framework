package org.hsweb.concurrent.lock.support;

import org.hsweb.concurrent.lock.LockFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 默认的锁工程。用于提供JDK自带锁创建
 * Created by zhouhao on 16-4-27.
 */
public class DefaultReadWriteLockFactory implements LockFactory {
    protected ConcurrentMap<String, ReadWriteLock> READ_WRITE_LOCK_BASE = new ConcurrentHashMap<>();
    protected ConcurrentMap<String, Lock> LOCK_BASE = new ConcurrentHashMap<>();


    @Override
    public ReadWriteLock createReadWriteLock(String key) {
        synchronized (READ_WRITE_LOCK_BASE) {
            ReadWriteLock lock = READ_WRITE_LOCK_BASE.get(key);
            if (lock == null) {
                READ_WRITE_LOCK_BASE.put(key, lock = new ReentrantReadWriteLock());
            }
            return lock;
        }
    }

    public Lock createLock(String key) {
        synchronized (LOCK_BASE) {
            Lock lock = LOCK_BASE.get(key);
            if (lock == null) {
                LOCK_BASE.put(key, lock = new ReentrantLock());
            }
            return lock;
        }
    }
}
