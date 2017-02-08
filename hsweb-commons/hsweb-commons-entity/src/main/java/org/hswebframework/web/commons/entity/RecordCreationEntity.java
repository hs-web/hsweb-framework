package org.hswebframework.web.commons.entity;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface RecordCreationEntity extends Entity {
    String getCreatorId();

    void setCreatorId(String creatorId);

    Long getCreateTime();

    void setCreateTime(Long createTime);

    default void setCreateTimeNow() {
        setCreateTime(System.currentTimeMillis());
    }
}
