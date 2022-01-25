package org.hswebframework.web.api.crud.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 记录修改信息的实体类,包括修改人和修改时间。
 *
 * @author zhouhao
 * @since 3.0.6
 */
public interface RecordModifierEntity extends Entity {

    String modifierId = "modifierId";
    String modifyTime = "modifyTime";

    /**
     * 修改人ID
     *
     * @return 修改人ID
     */
    String getModifierId();

    /**
     * 设置修改人ID
     *
     * @param modifierId 修改人ID
     */
    void setModifierId(String modifierId);

    /**
     * 设置修改人名字,为了兼容,默认不支持记录修改人名字,由具体的实现类进行实现
     *
     * @param modifierName 修改人名字
     */
    default void setModifierName(String modifierName) {

    }

    /**
     * @return 修改时间
     */
    Long getModifyTime();

    /**
     * 设置修改时间,UTC时间戳
     *
     * @param modifyTime 修改时间
     * @see System#currentTimeMillis()
     */
    void setModifyTime(Long modifyTime);

    /**
     * 设置修改时间为当前时间
     */
    default void setModifyTimeNow() {
        setModifyTime(System.currentTimeMillis());
    }

    /**
     * @deprecated 已弃用, 4.1版本中移除
     */
    @JsonIgnore
    default String getModifierIdProperty() {
        return modifierId;
    }
}
