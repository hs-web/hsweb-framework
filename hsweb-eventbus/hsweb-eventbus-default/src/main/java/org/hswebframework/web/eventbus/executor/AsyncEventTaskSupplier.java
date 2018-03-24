package org.hswebframework.web.eventbus.executor;

import org.hswebframework.web.eventbus.EventListener;
import org.hswebframework.web.eventbus.EventListenerDefine;
import org.hswebframework.web.eventbus.annotation.EventMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhouhao
 * @since 3.0
 */
public class AsyncEventTaskSupplier implements EventExecuteTaskSupplier {

    private static final ExecutorService executorService;

    static {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    @Override
    public boolean isSupport(EventListenerDefine define) {
        return define.getEventMode() == EventMode.ASYNC && !define.isTransaction();
    }

    @Override
    public EventExecuteTask get(EventListener listener, Object event) {
        return () -> executorService.execute(() -> listener.onEvent(event));
    }
}
