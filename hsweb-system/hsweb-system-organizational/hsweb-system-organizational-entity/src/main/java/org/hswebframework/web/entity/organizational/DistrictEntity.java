package org.hswebframework.web.entity.organizational;

import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.commons.entity.TreeSortSupportEntity;

import java.util.List;

/**
 * 行政区域 实体
 *
 * @author hsweb-generator-online
 */
public interface DistrictEntity extends TreeSortSupportEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 区域名称,如重庆市
     */
    String name      = "name";
    /**
     * 区域全程,如重庆市江津区
     */
    String fullName  = "fullName";
    /**
     * 区域级别名称,如:省
     */
    String levelName = "levelName";
    /**
     * 区域级别编码,如:province
     */
    String levelCode = "levelCode";
    /**
     * 行政区域代码,如:500000
     */
    String code      = "code";
    /**
     * 树路径,如: asb3-lsat
     */
    String path      = "path";
    /**
     * 说明
     */
    String describe  = "describe";
    /**
     * 状态
     */
    String status    = "status";

    /**
     * @return 区域名称, 如重庆市
     */
    String getName();

    /**
     * @param name 区域名称,如重庆市
     */
    void setName(String name);

    /**
     * @return 区域全称, 如重庆市江津区
     */
    String getFullName();

    /**
     * @param fullName 区域全程,如重庆市江津区
     */
    void setFullName(String fullName);

    /**
     * @return 区域级别名称, 如:省
     */
    String getLevelName();

    /**
     * @param levelName 区域级别名称,如:省
     */
    void setLevelName(String levelName);

    /**
     * @return 区域级别编码, 如:province
     */
    String getLevelCode();

    /**
     * @param levelCode 区域级别编码,如:province
     */
    void setLevelCode(String levelCode);

    /**
     * @return 行政区域代码, 如:500000
     */
    String getCode();

    /**
     * @param code 行政区域代码,如:500000
     */
    void setCode(String code);


    /**
     * @return 说明
     */
    String getDescribe();

    /**
     * @param describe 说明
     */
    void setDescribe(String describe);

    /**
     * @return 状态
     */
    Byte getStatus();

    /**
     * @param status 状态
     */
    void setStatus(Byte status);

    void setChildren(List<DistrictEntity> children);

    List<DistrictEntity> getChildren();

}