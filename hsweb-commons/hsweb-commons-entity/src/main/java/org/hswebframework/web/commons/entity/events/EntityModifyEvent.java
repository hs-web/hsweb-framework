package org.hswebframework.web.commons.entity.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class EntityModifyEvent<E> implements Serializable{

    private static final long serialVersionUID = -7158901204884303777L;

    private E before;

    private E after;

    private Class<E> entityType;

}
