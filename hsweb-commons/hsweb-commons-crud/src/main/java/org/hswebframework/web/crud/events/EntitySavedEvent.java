package org.hswebframework.web.crud.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
 * @see org.hswebframework.web.crud.annotation.EnableEntityEvent
 * @param <E>
 */
@AllArgsConstructor
@Getter
public class EntitySavedEvent<E> implements Serializable {

    private List<E> entity;

    private Class<E> entityType;
}
