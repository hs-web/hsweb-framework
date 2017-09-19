package org.hswebframework.web.async;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.Callable;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SpringTransactionSupportJob<V> implements TransactionSupportJob<V> {

    private TransactionStatus transactionStatus;

    private TransactionTemplate transactionTemplate;

    private boolean rollback = false;

    private Callable<V> target;

    public SpringTransactionSupportJob(TransactionTemplate transactionTemplate, Callable<V> job) {
        this.transactionTemplate = transactionTemplate;
        this.target = job;
    }

    @Override
    public void rollBackOnly() {
        rollback = true;
    }

    @Override
    public void commit() {
        //do noting
        if (transactionStatus != null) {
            if (rollback) {
                transactionTemplate.getTransactionManager().rollback(transactionStatus);
            } else {
                transactionTemplate.getTransactionManager().commit(transactionStatus);
            }
        }
    }

    @Override
    public V call() throws Exception {
        transactionStatus = transactionTemplate.getTransactionManager().getTransaction(transactionTemplate);
        if (rollback) transactionStatus.setRollbackOnly();
        return target.call();
    }
}
