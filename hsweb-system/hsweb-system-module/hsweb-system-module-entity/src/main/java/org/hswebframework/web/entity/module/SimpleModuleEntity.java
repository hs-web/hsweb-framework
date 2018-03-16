package org.hswebframework.web.entity.module;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * 系统自定义模块
 *
 * @author hsweb-generator-online
 */
public class SimpleModuleEntity extends SimpleGenericEntity<String> implements ModuleEntity {
    private static final long serialVersionUID = -25986777322199816L;
    //名称
    private String name;
    //权限id
    private String permissionId;
    //备注
    private String remark;
    //列表配置
    private String listMeta;
    //保存页配置
    private String saveMeta;
    //状态
    private Long   status;

    /**
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 权限id
     */
    public String getPermissionId() {
        return this.permissionId;
    }

    /**
     * @param permissionId 权限id
     */
    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    /**
     * @return 备注
     */
    public String getRemark() {
        return this.remark;
    }

    /**
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * @return 列表配置
     */
    public String getListMeta() {
        return this.listMeta;
    }

    /**
     * @param listMeta 列表配置
     */
    public void setListMeta(String listMeta) {
        this.listMeta = listMeta;
    }

    /**
     * @return 保存页配置
     */
    public String getSaveMeta() {
        return this.saveMeta;
    }

    /**
     * @param saveMeta 保存页配置
     */
    public void setSaveMeta(String saveMeta) {
        this.saveMeta = saveMeta;
    }

    /**
     * @return 状态
     */
    public Long getStatus() {
        return this.status;
    }

    /**
     * @param status 状态
     */
    public void setStatus(Long status) {
        this.status = status;
    }
}