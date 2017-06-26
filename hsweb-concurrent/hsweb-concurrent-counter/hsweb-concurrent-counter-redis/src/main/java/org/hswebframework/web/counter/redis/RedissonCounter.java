package org.hswebframework.web.counter.redis;

import org.hswebframework.web.concurrent.counter.Counter;
import org.redisson.api.RAtomicLong;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class RedissonCounter implements Counter {

    private final RAtomicLong atomicLong;

    public RedissonCounter(RAtomicLong rAtomicLong) {
        this.atomicLong = rAtomicLong;
    }

    @Override
    public long get() {
        return atomicLong.get();
    }

    @Override
    public void set(long num) {
        atomicLong.set(num);
    }

    @Override
    public long getAndAdd(long num) {
        return atomicLong.getAndAdd(num);
    }

    @Override
    public void add(long num) {
        atomicLong.addAndGet(num);
    }

    @Override
    public void increment() {
        atomicLong.incrementAndGet();
    }

    @Override
    public void decrement() {
        atomicLong.decrementAndGet();
    }

    @Override
    public long incrementAndGet() {
        return atomicLong.incrementAndGet();
    }

    @Override
    public long decrementAndGet() {
        return atomicLong.decrementAndGet();
    }
}
