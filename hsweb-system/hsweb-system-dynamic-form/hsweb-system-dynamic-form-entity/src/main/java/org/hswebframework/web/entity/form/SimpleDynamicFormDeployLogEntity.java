package org.hswebframework.web.entity.form;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * 表单发布日志
 *
 * @author hsweb-generator-online
 */
public class SimpleDynamicFormDeployLogEntity extends SimpleGenericEntity<String> implements DynamicFormDeployLogEntity {
    //表单ID
    private String formId;
    //发布的版本
    private Long   version;
    //发布时间
    private Long   deployTime;
    //部署的元数据
    private String metaData;
    //部署状态
    private Byte status;

    /**
     * @return 表单ID
     */
    @Override
    public String getFormId() {
        return this.formId;
    }

    /**
     * @param formId 表单ID
     */
    @Override
    public void setFormId(String formId) {
        this.formId = formId;
    }

    /**
     * @return 发布的版本
     */
    @Override
    public Long getVersion() {
        return this.version;
    }

    /**
     * @param version 发布的版本
     */
    @Override
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * @return 发布时间
     */
    @Override
    public Long getDeployTime() {
        return this.deployTime;
    }

    /**
     * @param deployTime 发布时间
     */
    @Override
    public void setDeployTime(Long deployTime) {
        this.deployTime = deployTime;
    }

    /**
     * @return 部署的元数据
     */
    @Override
    public String getMetaData() {
        return this.metaData;
    }

    /**
     * @param metaData 部署的元数据
     */
    @Override
    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    @Override
    public Byte getStatus() {
        return status;
    }

    @Override
    public void setStatus(Byte status) {
        this.status = status;
    }
}