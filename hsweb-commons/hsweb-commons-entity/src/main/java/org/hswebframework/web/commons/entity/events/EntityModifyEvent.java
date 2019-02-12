package org.hswebframework.web.commons.entity.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class EntityModifyEvent<E> implements Serializable{

    private E before;

    private E after;

}
