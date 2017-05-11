package org.hswebframework.web.concurrent.counter;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleCounterManager extends AbstractCounterManager {
    @Override
    protected Counter createCount(String name) {
        return new SimpleCounter();
    }
}
