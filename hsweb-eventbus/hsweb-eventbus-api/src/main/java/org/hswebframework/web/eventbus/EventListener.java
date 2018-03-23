package org.hswebframework.web.eventbus;

/**
 * @author zhouhao
 * @since 1.0
 */
public interface EventListener<E> {
    void onEvent(E event);
}
