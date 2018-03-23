package org.hswebframework.web.eventbus.executor;

import org.hswebframework.web.eventbus.EventListener;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface EventListenerExecutor {
    <E> void doExecute(EventListener<E> listener, E event);
}
