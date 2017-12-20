package org.hswebframework.web.async;

import java.util.concurrent.Callable;

/**
 * 支持事务的任务包装器
 *
 * @author zhouhao
 */
public interface TransactionSupportJobWrapper {
    /**
     * 将一个普通任务包装为支持事务的任务
     *
     * @param callable 普通任务
     * @param <V>      任务返回值类型
     * @return 支持事务的任务
     */
    <V> TransactionSupportJob<V> wrapper(Callable<V> callable);
}
