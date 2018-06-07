package org.hswebframework.web.entity.organizational;

import org.hswebframework.web.commons.entity.GenericEntity;

/**
 * 关系定义 实体
 *
 * @author hsweb-generator-online
 */
public interface RelationDefineEntity extends GenericEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 关系名称
     */
    String name   = "name";
    /**
     * 关系类型ID
     */
    String typeId = "typeId";
    /**
     * 状态
     */
    String status = "status";

    /**
     * @return 关系名称
     */
    String getName();

    /**
     * @param name 关系名称
     */
    void setName(String name);

    /**
     * @return 关系类型ID
     */
    String getTypeId();

    /**
     * @param typeId 关系类型ID
     */
    void setTypeId(String typeId);

    /**
     * @return 状态
     */
    Byte getStatus();

    /**
     * @param status 状态
     */
    void setStatus(Byte status);

}