package org.hswebframework.web.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
public class TranslationBatchAsyncJobContainer implements BatchAsyncJobContainer {

    private ExecutorService executorService;

    private List<Exception> exceptions = new ArrayList<>();

    private Supplier<TranslationSupportJobWrapper> supportJobSupplierBuilder;

    private CountDownLatch downLatch = new CountDownLatch(1);

    private AtomicInteger failCounter = new AtomicInteger();

    private AtomicInteger overCounter = new AtomicInteger();

    private List<Future> futures = new ArrayList<>();

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public <V> BatchAsyncJobContainer submit(Callable<V> callable) {
        TranslationSupportJob<V> translationJob = supportJobSupplierBuilder
                .get()
                .wrapper(callable);
        Callable<V> proxy = () -> {
            try {
                if (failCounter.get() > 0) {
                    return null;
                }
                V val = translationJob.call();
                overCounter.incrementAndGet();
                downLatch.await();
                if (failCounter.get() > 0) {
                    translationJob.rollBack();
                    return null;
                }
                return val;
            } catch (Exception e) {
                exceptions.add(e);
                failCounter.incrementAndGet();
                overCounter.incrementAndGet();
            }
            return null;
        };
        futures.add(executorService.submit(proxy));
        return this;
    }

    @Override
    public List<Object> getResult() throws Exception {
        while (overCounter.get() != futures.size()) {
            Thread.sleep(10);
        }
        downLatch.countDown();
        if (!exceptions.isEmpty()) {
            throw new RuntimeException();
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
