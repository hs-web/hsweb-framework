package org.hswebframework.web.concurrent.counter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 */
public abstract class AbstractCounterManager implements CounterManager {

    private final Map<String, Counter> counterStore = new HashMap<>(128);

    @Override
    public Counter getCounter(String name, long initValue) {
        Counter counter = counterStore.get(name);
        if (counter != null)
            return counter;
        synchronized (counterStore) {
            return counterStore.computeIfAbsent(name, k -> this.createCount(name, initValue));
        }
    }

    protected abstract Counter createCount(String name, long initValue);
}
