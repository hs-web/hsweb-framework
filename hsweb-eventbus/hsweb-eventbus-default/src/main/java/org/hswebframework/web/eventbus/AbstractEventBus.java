package org.hswebframework.web.eventbus;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.eventbus.executor.EventListenerContainer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhouhao
 * @since 1.0
 */
@Slf4j
public abstract class AbstractEventBus implements EventBus, EventSubscriber {
    private ConcurrentMap<String, EventListenerContainer> listenerStorage = new ConcurrentHashMap<>();

    @Override
    public void publish(Object event) {
        EventListenerContainer container = listenerStorage.get(getKey(event));
        if (null != container) {
            container.doExecute(event);
        } else {
            log.warn("没有监听器处理此事件:{}", event);
        }
    }

    public String getKey(Object event) {
        return getKey(event.getClass());
    }

    public String getKey(Class eventType) {
        return eventType.getName();
    }

    @Override
    public <E> void subscribe(Class<E> eventType, EventListenerDefine define) {
        EventListenerContainer container = listenerStorage
                .computeIfAbsent(getKey(eventType), type -> createEventListenerContainer());
        container.addListener(define);
    }

    protected abstract EventListenerContainer createEventListenerContainer();

}
