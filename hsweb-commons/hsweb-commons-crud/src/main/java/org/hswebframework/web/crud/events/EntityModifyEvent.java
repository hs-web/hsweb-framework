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
public class EntityModifyEvent<E> extends DefaultAsyncEvent implements Serializable{

    private static final long serialVersionUID = -7158901204884303777L;

    private final List<E> before;

    private final List<E> after;

    private final Class<E> entityType;

}
