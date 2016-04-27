package org.hsweb.concurrent.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * 锁工厂接口，通过此接口可创建锁和读写锁
 * Created by zhouhao on 16-4-27.
 */
public interface LockFactory {
    /**
     * 创建一个指定key的读写锁，相同的key，将得到同一个锁对象
     *
     * @param key 锁标识
     * @return 锁对象
     */
    ReadWriteLock createReadWriteLock(String key);

    /**
     * 创建一个指定key的锁，相同的key，将得到同一个锁对象
     *
     * @param key 锁标识
     * @return 锁对象
     */
    Lock createLock(String key);
}
