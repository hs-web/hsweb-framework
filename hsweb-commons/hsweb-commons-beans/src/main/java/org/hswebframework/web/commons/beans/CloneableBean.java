package org.hswebframework.web.commons.beans;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface CloneableBean extends Bean, Cloneable {
    CloneableBean clone();
}
