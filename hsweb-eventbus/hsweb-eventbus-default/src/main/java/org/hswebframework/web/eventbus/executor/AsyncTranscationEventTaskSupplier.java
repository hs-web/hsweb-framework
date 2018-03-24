package org.hswebframework.web.eventbus.executor;

import org.hswebframework.web.async.BatchAsyncJobContainer;
import org.hswebframework.web.eventbus.EventListener;
import org.hswebframework.web.eventbus.EventListenerDefine;
import org.hswebframework.web.eventbus.annotation.EventMode;


/**
 * @author zhouhao
 * @since 3.0
 */
public class AsyncTranscationEventTaskSupplier implements EventExecuteTaskSupplier {

    public AsyncTranscationEventTaskSupplier(BatchAsyncJobContainer container) {
        this.container = container;
    }

    private BatchAsyncJobContainer container;

    @Override
    public boolean isSupport(EventListenerDefine define) {
        return define.getEventMode() == EventMode.ASYNC && define.isTransaction();
    }

    @Override
    public EventExecuteTask get(EventListener listener, Object event) {
        return () -> container.submit(() -> {
            listener.onEvent(event);
            return true;
        }, true);
    }
}
