package org.hswebframework.web.concurrent.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public abstract class AbstactLocakFactory implements LockFactory {
    private final Map<String, Lock>          lockStore          = new HashMap<>(128);
    private final Map<String, ReadWriteLock> readWriteLockStore = new HashMap<>(128);

    @Override
    public Lock getLock(String lockKey) {
        Lock lock = lockStore.get(lockKey);
        if (lock != null) return lock;
        synchronized (lockStore) {
            return lockStore.computeIfAbsent(lockKey, this::createLock);
        }
    }

    @Override
    public ReadWriteLock getReadWriteLock(String lockKey) {
        ReadWriteLock lock = readWriteLockStore.get(lockKey);
        if (lock != null) return lock;
        synchronized (readWriteLockStore) {
            return readWriteLockStore.computeIfAbsent(lockKey, this::createReadWriteLock);
        }
    }

    protected abstract Lock createLock(String lockKey);

    protected abstract ReadWriteLock createReadWriteLock(String lockKey);

}
