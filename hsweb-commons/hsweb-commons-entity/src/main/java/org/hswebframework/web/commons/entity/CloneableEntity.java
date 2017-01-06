package org.hswebframework.web.commons.entity;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface CloneableEntity extends Entity, Cloneable {
    <T extends CloneableEntity> T clone();
}
