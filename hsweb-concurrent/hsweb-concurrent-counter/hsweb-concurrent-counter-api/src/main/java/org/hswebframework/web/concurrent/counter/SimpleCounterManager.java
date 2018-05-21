package org.hswebframework.web.concurrent.counter;

/**
 * @author zhouhao
 */
public class SimpleCounterManager extends AbstractCounterManager {
    @Override
    protected Counter createCount(String name) {
        return new SimpleCounter();
    }
}
