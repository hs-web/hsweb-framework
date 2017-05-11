package org.hswebframework.web.concurrent.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * 锁工厂,用于创建获取锁
 *
 * @author zhouhao
 * @see Lock
 * @see ReadWriteLock
 * @since 3.0
 */
public interface LockFactory {
    Lock getLock(String lockKey);

    ReadWriteLock getReadWriteLock(String lockKey);
}
