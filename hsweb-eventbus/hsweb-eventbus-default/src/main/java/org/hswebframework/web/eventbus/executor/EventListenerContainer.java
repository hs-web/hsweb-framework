package org.hswebframework.web.eventbus.executor;


/**
 * @author zhouhao
 * @since 3.0
 */
public interface EventListenerContainer {
    void doExecute(Object event);

    void addListener(EventListenerDefine listener);
}
