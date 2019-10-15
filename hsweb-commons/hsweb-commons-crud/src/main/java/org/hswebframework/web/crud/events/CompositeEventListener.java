package org.hswebframework.web.crud.events;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.events.EventContext;
import org.hswebframework.ezorm.rdb.events.EventListener;
import org.hswebframework.ezorm.rdb.events.EventType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class CompositeEventListener implements EventListener {

    private List<EventListener> eventListeners = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(EventType type, EventContext context) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onEvent(type, context);
        }
    }

    public void addListener(EventListener eventListener) {
        eventListeners.add(eventListener);
    }
}
