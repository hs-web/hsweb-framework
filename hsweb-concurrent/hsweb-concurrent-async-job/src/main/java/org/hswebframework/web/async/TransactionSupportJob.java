package org.hswebframework.web.async;

import java.util.concurrent.Callable;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface TransactionSupportJob<V> extends Callable<V> {

    void rollBackOnly();

    void commit();
}
