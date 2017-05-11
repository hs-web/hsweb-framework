package org.hswebframework.web.concurrent.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public abstract class AbstactLocakFactory implements LockFactory {
    private Map<String, Lock>          lockStore          = new ConcurrentHashMap<>(128);
    private Map<String, ReadWriteLock> readWriteLockStore = new ConcurrentHashMap<>(128);

    @Override
    public Lock getLock(String lockKey) {
        return lockStore.computeIfAbsent(lockKey, this::createLock);
    }

    @Override
    public ReadWriteLock getReadWriteLock(String lockKey) {
        return readWriteLockStore.computeIfAbsent(lockKey, this::createReadWriteLock);
    }

    protected abstract Lock createLock(String lockKey);

    protected abstract ReadWriteLock createReadWriteLock(String lockKey);

}
