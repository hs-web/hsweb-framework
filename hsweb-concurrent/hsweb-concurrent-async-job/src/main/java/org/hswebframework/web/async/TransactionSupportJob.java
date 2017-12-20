package org.hswebframework.web.async;

import java.util.concurrent.Callable;

/**
 * 支持事务的异步任务
 * @author zhouhao
 */
public interface TransactionSupportJob<V> extends Callable<V> {

    /**
     * 回滚
     */
    void rollBackOnly();

    /**
     * 提交事务
     */
    void commit();
}
