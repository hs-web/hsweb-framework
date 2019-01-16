package org.hswebframework.web.commons.entity;

/**
 * 记录创建信息的实体类,包括创建人和创建时间。
 * 此实体类与行级权限控制相关联:只能操作自己创建的数据
 *
 * @author zhouhao
 * @since 3.0
 */
public interface RecordCreationEntity extends Entity {

    String creatorId  = "creatorId";
    String createTime = "createTime";

    String getCreatorId();

    void setCreatorId(String creatorId);

    Long getCreateTime();

    void setCreateTime(Long createTime);

    default void setCreateTimeNow() {
        setCreateTime(System.currentTimeMillis());
    }

    default String getCreatorIdProperty() {
        return creatorId;
    }
}
