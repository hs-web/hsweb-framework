package org.hswebframework.web.eventbus.executor;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.eventbus.EventListenerDefine;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author zhouhao
 * @since 1.0
 */
@Slf4j
public abstract class AbstractEventContainer implements EventListenerContainer {

    protected final List<EventListenerDefine> defines = new ArrayList<>();

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    protected abstract EventListenerExecutor newExecutor();

    protected abstract void executeAfter(EventListenerExecutor executor);

    @Override
    public void doExecute(Object event) {
        lock.readLock().lock();
        try {
            EventListenerExecutor executor = newExecutor();
            defines.forEach(define -> executor.doExecute(define, event));
            executeAfter(executor);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void addListener(EventListenerDefine listener) {
        lock.writeLock().lock();
        try {
            defines.add(listener);
            defines.sort(Comparator.comparingInt(EventListenerDefine::getPriority));
        } finally {
            lock.writeLock().unlock();
        }
    }

}
