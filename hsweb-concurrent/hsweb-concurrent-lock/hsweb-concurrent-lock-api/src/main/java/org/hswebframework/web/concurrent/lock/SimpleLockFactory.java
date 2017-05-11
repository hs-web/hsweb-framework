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
 * @see AbstactLocakFactory
 * @since 3.0
 */
public class SimpleLockFactory extends AbstactLocakFactory {
    @Override
    protected synchronized Lock createLock(String lockKey) {
        return new ReentrantLock();
    }

    @Override
    protected synchronized ReadWriteLock createReadWriteLock(String lockKey) {
        return new ReentrantReadWriteLock();
    }
}
