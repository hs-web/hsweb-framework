package org.hswebframework.web.eventbus.executor;

import org.hswebframework.web.eventbus.EventListener;
import org.hswebframework.web.eventbus.EventListenerDefine;
import org.hswebframework.web.eventbus.annotation.EventMode;

/**
 * @author zhouhao
 * @since 1.0
 */
public class SyncEventListenerExecutor implements EventExecuteTaskSupplier {
    @Override
    public boolean isSupport(EventListenerDefine define) {
        return define.getEventMode() == EventMode.SYNC;
    }

    @Override
    public EventExecuteTask get(EventListener listener, Object event) {
        return () -> listener.onEvent(event);
    }
}
