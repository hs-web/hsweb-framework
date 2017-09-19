package org.hswebframework.web.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.Callable;

/**
 * @author zhouhao
 */
public class SpringTransactionSupportJobWrapper implements TransactionSupportJobWrapper {

    private TransactionTemplate transactionTemplate;

    @Autowired
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <V> TransactionSupportJob<V> wrapper(Callable<V> callable) {
        return new SpringTransactionSupportJob<>(transactionTemplate, callable);
    }
}
