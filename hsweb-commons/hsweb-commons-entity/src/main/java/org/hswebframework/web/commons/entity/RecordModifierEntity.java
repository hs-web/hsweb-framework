package org.hswebframework.web.commons.entity;

/**
 * 记录修改信息的实体类,包括修改人和修改时间。
 *
 * @author zhouhao
 * @since 3.0.6
 */
public interface RecordModifierEntity extends Entity {

    String modifierId = "modifierId";
    String modifyTime = "modifyTime";

    String getModifierId();

    void setModifierId(String modifierId);

    Long getModifyTime();

    void setModifyTime(Long modifyTime);

    default void setModifyTimeNow() {
        setModifyTime(System.currentTimeMillis());
    }

    default String getModifierIdProperty() {
        return modifierId;
    }
}
