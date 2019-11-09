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
public class EntityModifyEvent<E> implements Serializable{

    private static final long serialVersionUID = -7158901204884303777L;

    private List<E> before;

    private List<E> after;

    private Class<E> entityType;

}
