package org.hswebframework.web.crud.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.event.DefaultAsyncEvent;

import java.io.Serializable;
import java.util.List;

/**
 * @see org.hswebframework.web.crud.annotation.EnableEntityEvent
 * @param <E>
 */
@AllArgsConstructor
@Getter
public class EntityBeforeCreateEvent<E> extends DefaultAsyncEvent implements Serializable {

    private final List<E> entity;

    private final Class<E> entityType;

    @Override
    public String toString() {
        return "EntityBeforeCreateEvent<" + entityType.getSimpleName() + ">"+entity;
    }
}
