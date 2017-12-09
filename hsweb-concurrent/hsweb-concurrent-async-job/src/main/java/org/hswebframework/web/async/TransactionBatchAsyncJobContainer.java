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

    private ExecutorService executorService;
    private TransactionSupportJobWrapper translationSupportJobWrapper;
    private static final Logger logger = LoggerFactory.getLogger(TransactionBatchAsyncJobContainer.class);

    public TransactionBatchAsyncJobContainer(ExecutorService executorService, TransactionSupportJobWrapper translationSupportJobWrapper) {
        this.executorService = executorService;
        this.translationSupportJobWrapper = translationSupportJobWrapper;
    }

    private List<Exception> exceptions = new ArrayList<>();

    private AtomicInteger failCounter = new AtomicInteger();

    private AtomicInteger transactionJobOverCounter = new AtomicInteger(0);

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private List<Future> futures = new ArrayList<>();

    private AtomicInteger transactionJobNumber = new AtomicInteger(0);

    private volatile boolean shutdown = false;

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public <V> BatchAsyncJobContainer submit(Callable<V> callable, boolean enableTransaction) {
        if (shutdown) {
            logger.warn("TransactionBatchAsyncJobContainer is shutdown, fail job number :{}", failCounter.get());
            return this;
        }
        if (!enableTransaction) {
            if (logger.isDebugEnabled()) {
                logger.debug("submit not transaction support job");
            }
            futures.add(executorService.submit(() -> {
                if (shutdown) {
                    return null;
                }
                return callable.call();
            }));
            return this;
        }

        int tmpJobFlag = transactionJobNumber.incrementAndGet();

        if (logger.isDebugEnabled()) {
            logger.debug("submit transaction support job {}", transactionJobNumber);
        }

        TransactionSupportJob<V> translationJob = translationSupportJobWrapper.wrapper(callable);
        Callable<V> proxy = () -> {
            V value = null;
            try {
                if (failCounter.get() > 0 || shutdown) {
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
                logger.warn("transaction support job {} fail.", tmpJobFlag, e);
            }finally {
                transactionJobOverCounter.incrementAndGet();
            }
            return value;
        };
        futures.add(executorService.submit(proxy));
        return this;
    }

    @Override
    public List<Object> getResult() throws Exception {
        while (transactionJobOverCounter.get() != transactionJobNumber.get() && failCounter.get() == 0) {
            Thread.sleep(50);
        }
        countDownLatch.countDown();

        List<Object> results = futures.stream().map(this::getValue).collect(Collectors.toList());
        if (!exceptions.isEmpty()) {
            throw new AsyncJobException(exceptions);
        }
        return results;
    }

    private Object getValue(Future future) {
        if(future==null){return null;}
        try {
            return future.get();
        } catch (Exception e) {
            exceptions.add(e);
        }
        return null;
    }

    @Override
    public BatchAsyncJobContainer cancel() {
        shutdown = true;
        return this;
    }
}
