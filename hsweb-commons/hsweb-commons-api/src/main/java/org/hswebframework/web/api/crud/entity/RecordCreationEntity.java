package org.hswebframework.web.api.crud.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 记录创建信息的实体类,包括创建人和创建时间。
 * 此实体类与行级权限控制相关联:只能操作自己创建的数据
 *
 * @author zhouhao
 * @since 3.0
 */
public interface RecordCreationEntity extends Entity {

    /**
     * @return 创建者ID
     */
    String getCreatorId();

    /**
     * 设置创建者ID
     *
     * @param creatorId 创建者ID
     */
    void setCreatorId(String creatorId);

    /**
     * 创建时间,UTC时间戳
     *
     * @return 创建时间
     * @see System#currentTimeMillis()
     */
    Long getCreateTime();

    /**
     * 设置创建时间 ,UTC时间戳
     *
     * @param createTime 创建时间
     * @see System#currentTimeMillis()
     */
    void setCreateTime(Long createTime);

    /**
     * 设置创建者名字,为了兼容,默认不支持记录创建者名字,由具体的实现类进行实现
     *
     * @param name 创建者名字
     */
    default void setCreatorName(String name) {

    }

    /**
     * 设置创建时间为当前时间
     */
    default void setCreateTimeNow() {
        setCreateTime(System.currentTimeMillis());
    }

    /**
     * @deprecated 已弃用, 在4.1版本中移除
     */
    @JsonIgnore
    @Deprecated
    default String getCreatorIdProperty() {
        return "creatorId";
    }
}
