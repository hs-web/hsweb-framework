package org.hswebframework.web.eventbus;

/**
 * @author zhouhao
 * @since 1.0
 */
public interface EventSubscriber {
    <E> void subscribe(Class<E> eventType, EventListenerDefine listener);
}
