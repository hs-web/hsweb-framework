package org.hswebframework.web.entity.form;

import org.hswebframework.web.commons.entity.GenericEntity;

/**
 * 表单发布日志 实体
 *
 * @author hsweb-generator-online
 */
public interface DynamicFormDeployLogEntity extends GenericEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 表单ID
     */
    String formId     = "formId";
    /**
     * 发布的版本
     */
    String version    = "version";
    /**
     * 发布时间
     */
    String deployTime = "deployTime";
    /**
     * 部署的元数据
     */
    String metaData   = "metaData";
    /**
     * 部署状态
     */
    String status     = "status";

    /**
     * @return 表单ID
     */
    String getFormId();

    /**
     * @param formId 表单ID
     */
    void setFormId(String formId);

    /**
     * @return 发布的版本
     */
    Long getVersion();

    /**
     * @param version 发布的版本
     */
    void setVersion(Long version);

    /**
     * @return 发布时间
     */
    Long getDeployTime();

    /**
     * @param deployTime 发布时间
     */
    void setDeployTime(Long deployTime);

    /**
     * @return 部署的元数据
     */
    String getMetaData();

    /**
     * @param metaData 部署的元数据
     */
    void setMetaData(String metaData);

    Byte getStatus();

    void setStatus(Byte status);
}