package org.hswebframework.web.eventbus.executor;

import org.hswebframework.web.eventbus.EventListener;
import org.hswebframework.web.eventbus.EventListenerDefine;

/**
 * @author zhouhao
 * @since 1.0
 */
public interface EventExecuteTaskSupplier {
    boolean isSupport(EventListenerDefine define);

    EventExecuteTask get(EventListener listener, Object event);

   default void close(){}
}
