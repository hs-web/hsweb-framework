package org.hswebframework.web.counter.redis;

import org.hswebframework.web.concurrent.counter.AbstractCounterManager;
import org.hswebframework.web.concurrent.counter.Counter;
import org.hswebframework.web.concurrent.counter.CounterManager;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;

import java.util.Map;

/**
 * @author zhouhao
 */
public class RedissonCounterManager extends AbstractCounterManager {

    private Redisson redisson;

    public RedissonCounterManager(Redisson redisson) {
        this.redisson = redisson;
    }

    @Override
    protected Counter createCount(String name) {
        return new RedissonCounter(redisson.getAtomicLong(name));
    }
}
