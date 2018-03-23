package org.hswebframework.web.eventbus;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.utils.ClassUtils;
import org.hswebframework.web.eventbus.executor.EventListenerContainer;
import org.hswebframework.web.eventbus.executor.EventListenerDefine;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhouhao
 * @since 1.0
 */
@Slf4j
public abstract class DefaultEventBus implements EventBus, EventSubscriber {
    private ConcurrentMap<String, EventListenerContainer> listenerStorage = new ConcurrentHashMap<>();

    @Override
    public void publish(Object event) {
        EventListenerContainer container = listenerStorage.get(getKey(event));
        if (null != container) {
            container.doExecute(event);
        } else {
            log.warn("event:{},not support!", event);
        }
    }

    public String getKey(Object event) {
        return getKey(event.getClass());
    }

    public String getKey(Class eventType) {
        return eventType.getName();
    }

    @Override
    public <E> void subscribe(EventListener<E> listener) {
        Class<E> type = (Class<E>) ClassUtils.getGenericType(listener.getClass());
        if (type == Object.class) {
            throw new UnsupportedOperationException("event type [Object] not support!");
        }
        subscribe(type, listener);
    }

    @Override
    public <E> void subscribe(Class<E> eventType, EventListener<? extends E> listener) {
        EventListenerContainer container = listenerStorage
                .computeIfAbsent(getKey(eventType), type -> createEventListenerContainer(listener));
        container.addListener(createListenerDefine(listener));
    }

    protected abstract EventListenerContainer createEventListenerContainer(EventListener listener);

    protected abstract EventListenerDefine createListenerDefine(EventListener listener);

}
