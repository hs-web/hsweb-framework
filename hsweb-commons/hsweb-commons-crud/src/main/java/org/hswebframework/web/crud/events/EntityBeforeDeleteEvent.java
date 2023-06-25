package org.hswebframework.web.crud.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.event.DefaultAsyncEvent;

import java.io.Serializable;
import java.util.List;

/**
 * @param <E>
 * @see org.hswebframework.web.crud.annotation.EnableEntityEvent
 */
@AllArgsConstructor
@Getter
public class EntityBeforeDeleteEvent<E> extends DefaultAsyncEvent implements Serializable {

    private static final long serialVersionUID = -7158901204884303777L;

    private final List<E> entity;

    private final Class<E> entityType;

    @Override
    public String toString() {
        return "EntityBeforeDeleteEvent<" + entityType.getSimpleName() + ">"+entity;
    }
}
