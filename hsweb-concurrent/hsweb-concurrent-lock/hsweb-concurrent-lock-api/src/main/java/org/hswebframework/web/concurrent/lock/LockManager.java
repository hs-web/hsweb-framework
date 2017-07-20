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
public interface LockManager {
    /**
     * 根据锁名称获取锁,相同的名称,则锁也相同
     *
     * @param lockName 锁名称
     * @return 锁对象
     * @see Lock
     */
    Lock getLock(String lockName);

    /**
     * 根据锁名称获取读写锁,相同的名称,则锁也相同
     *
     * @param lockName 锁名称
     * @return 读写锁对象
     * @see ReadWriteLock
     */
    ReadWriteLock getReadWriteLock(String lockName);
}
