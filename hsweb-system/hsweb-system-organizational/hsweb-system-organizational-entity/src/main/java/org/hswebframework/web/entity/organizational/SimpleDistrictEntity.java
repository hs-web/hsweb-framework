package org.hswebframework.web.entity.organizational;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;

import java.util.List;

/**
 * 表单发布日志
 *
 * @author hsweb-generator-online
 */
public class SimpleDistrictEntity extends SimpleTreeSortSupportEntity<String> implements DistrictEntity {
    //区域名称,如重庆市
    private String name;
    //区域全程,如重庆市江津区
    private String fullName;
    //区域级别名称,如:省
    private String levelName;
    //区域级别编码,如:province
    private String levelCode;
    //行政区域代码,如:500000
    private String code;
    //说明
    private String describe;
    //状态
    private Byte   status;

    private List<DistrictEntity> children;

    /**
     * @return 区域名称, 如重庆市
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name 区域名称,如重庆市
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 区域全称, 如重庆市江津区
     */
    public String getFullName() {
        return this.fullName;
    }

    /**
     * @param fullName 区域全称,如重庆市江津区
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return 区域级别名称, 如:省
     */
    public String getLevelName() {
        return this.levelName;
    }

    /**
     * @param levelName 区域级别名称,如:省
     */
    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    /**
     * @return 区域级别编码, 如:province
     */
    public String getLevelCode() {
        return this.levelCode;
    }

    /**
     * @param levelCode 区域级别编码,如:province
     */
    public void setLevelCode(String levelCode) {
        this.levelCode = levelCode;
    }

    /**
     * @return 行政区域代码, 如:500000
     */
    public String getCode() {
        return this.code;
    }

    /**
     * @param code 行政区域代码,如:500000
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return 说明
     */
    public String getDescribe() {
        return this.describe;
    }

    /**
     * @param describe 说明
     */
    public void setDescribe(String describe) {
        this.describe = describe;
    }

    /**
     * @return 状态
     */
    public Byte getStatus() {
        return this.status;
    }

    /**
     * @param status 状态
     */
    public void setStatus(Byte status) {
        this.status = status;
    }

    @Override
    public List<DistrictEntity> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<DistrictEntity> children) {
        this.children = children;
    }
}