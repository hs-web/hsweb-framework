package org.hswebframework.web.commons.entity.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class EntityCreatedEvent<E> implements Serializable {

    private E entity;

    private Class<E> entityType;
}
