package org.hswebframework.web.concurrent.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 使用jdk的锁实现
 *
 * @author zhouhao
 * @see ReentrantLock
 * @see ReentrantReadWriteLock
 * @see AbstractLockManager
 * @since 3.0
 */
public class SimpleLockManager extends AbstractLockManager {
    @Override
    protected Lock createLock(String lockName) {
        return new ReentrantLock();
    }

    @Override
    protected ReadWriteLock createReadWriteLock(String lockName) {
        return new ReentrantReadWriteLock();
    }
}
