package org.hswebframework.web.commons.entity;

/**
 * @author zhouhao
 */
public interface CloneableEntity extends Entity, Cloneable {
    CloneableEntity clone();
}
