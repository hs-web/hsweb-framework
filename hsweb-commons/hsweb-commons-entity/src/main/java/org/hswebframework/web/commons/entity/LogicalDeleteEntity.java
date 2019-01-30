package org.hswebframework.web.commons.entity;

/**
 * 逻辑删除
 *
 * @author zhouhao
 * @since 3.0.6
 */
public interface LogicalDeleteEntity {

    Boolean getDeleted();

    void setDeleted(boolean deleted);

    Long getDeleteTime();

    void setDeleteTime(Long deleteTime);

}
