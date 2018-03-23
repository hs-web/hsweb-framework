package org.hswebframework.web.eventbus;

import org.hswebframework.web.eventbus.executor.EventListenerExecutor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhouhao
 * @since 1.0
 */
public class DefaultEventPublisher implements EventPublisher {

    private ConcurrentMap<String, List<EventListenerExecutor>> listenerStorage = new ConcurrentHashMap<>();


    @Override
    public void publish(Object event) {

    }
}
