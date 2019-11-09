package org.hswebframework.web.crud.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @param <E>
 * @see org.hswebframework.web.crud.annotation.EnableEntityEvent
 */
@AllArgsConstructor
@Getter
public class EntityDeletedEvent<E> implements Serializable {

    private static final long serialVersionUID = -7158901204884303777L;

    private List<E> entity;

    private Class<E> entityType;

}
