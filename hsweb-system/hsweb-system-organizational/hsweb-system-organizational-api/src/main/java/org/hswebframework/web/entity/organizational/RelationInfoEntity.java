package org.hswebframework.web.entity.organizational;

import org.hswebframework.web.commons.entity.GenericEntity;

/**
 * 关系信息 实体
 *
 * @author hsweb-generator-online
 */
public interface RelationInfoEntity extends GenericEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 关系从
     */
    String relationFrom     = "relationFrom";
    /**
     * 关系定义id
     */
    String relationId       = "relationId";
    /**
     * 关系至
     */
    String relationTo       = "relationTo";
    /**
     * 关系类型从,如:人员
     */
    String relationTypeFrom = "relationTypeFrom";
    /**
     * 关系类型至,如:部门
     */
    String relationTypeTo   = "relationTypeTo";
    /**
     * 状态
     */
    String status           = "status";

    /**
     * @return 关系从
     */
    String getRelationFrom();

    /**
     * @param relationFrom 关系从
     */
    void setRelationFrom(String relationFrom);

    /**
     * @return 关系定义id
     */
    String getRelationId();

    /**
     * @param relationId 关系定义id
     */
    void setRelationId(String relationId);

    /**
     * @return 关系至
     */
    String getRelationTo();

    /**
     * @param relationTo 关系至
     */
    void setRelationTo(String relationTo);

    /**
     * @return 关系类型从, 如:人员
     */
    String getRelationTypeFrom();

    /**
     * @param relationTypeFrom 关系类型从,如:人员
     */
    void setRelationTypeFrom(String relationTypeFrom);

    /**
     * @return 关系类型至, 如:部门
     */
    String getRelationTypeTo();

    /**
     * @param relationTypeTo 关系类型至,如:部门
     */
    void setRelationTypeTo(String relationTypeTo);

    /**
     * @return 状态
     */
    Byte getStatus();

    /**
     * @param status 状态
     */
    void setStatus(Byte status);

}