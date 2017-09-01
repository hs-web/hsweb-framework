package org.hswebframework.web.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
public class TransactionBatchAsyncJobContainer implements BatchAsyncJobContainer {

    private ExecutorService              executorService;
    private TransactionSupportJobWrapper translationSupportJobWrapper;
    private static final Logger logger = LoggerFactory.getLogger(TransactionBatchAsyncJobContainer.class);

    public TransactionBatchAsyncJobContainer(ExecutorService executorService, TransactionSupportJobWrapper translationSupportJobWrapper) {
        this.executorService = executorService;
        this.translationSupportJobWrapper = translationSupportJobWrapper;
    }

    private List<Exception> exceptions = new ArrayList<>();

    private AtomicInteger failCounter = new AtomicInteger();

    private AtomicInteger transactionJobOverCounter = new AtomicInteger();

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private List<Future> futures = new ArrayList<>();

    private int transactionJobNumber = 0;

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public <V> BatchAsyncJobContainer submit(Callable<V> callable, boolean enableTransaction) {
        if (!enableTransaction) {
            if (logger.isDebugEnabled()) {
                logger.debug("submit not transaction support job {}", transactionJobNumber);
            }
            futures.add(executorService.submit(callable));
            return this;
        }
        transactionJobNumber++;
        if (logger.isDebugEnabled()) {
            logger.debug("submit transaction support job {}", transactionJobNumber);
        }
        int tmpJobFlag = transactionJobNumber;

        TransactionSupportJob<V> translationJob = translationSupportJobWrapper.wrapper(callable);
        Callable<V> proxy = () -> {
            V value = null;
            try {
                if (failCounter.get() > 0) {
                    return null;
                }
                value = translationJob.call();
                transactionJobOverCounter.incrementAndGet();
                if (logger.isDebugEnabled()) {
                    logger.debug("transaction support job {} success,wait...", tmpJobFlag);
                }
                //等待其他任务完成
                countDownLatch.await();
                if (failCounter.get() > 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("transaction support job {} success,but other job failed, do rollback only!", tmpJobFlag);
                    }
                    translationJob.rollBackOnly();
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("transaction support job {} success,commit.", tmpJobFlag);
                    }
                }
                translationJob.commit();
            } catch (Exception e) {
                exceptions.add(e);
                failCounter.incrementAndGet();
                transactionJobOverCounter.incrementAndGet();
                logger.warn("transaction support job {} fail.", tmpJobFlag, e);
            }
            return value;
        };
        futures.add(executorService.submit(proxy));
        return this;
    }

    @Override
    public List<Object> getResult() throws Exception {
        while (transactionJobOverCounter.get() != transactionJobNumber) {
            Thread.sleep(50);
        }
        countDownLatch.countDown();
        if (!exceptions.isEmpty()) {
            throw new AsyncJobException(exceptions);
        }

        return futures.stream().map(this::getValue).collect(Collectors.toList());
    }

    private Object getValue(Future future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
