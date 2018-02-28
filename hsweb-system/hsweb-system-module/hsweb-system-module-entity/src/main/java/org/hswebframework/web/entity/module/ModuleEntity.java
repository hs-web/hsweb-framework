package org.hswebframework.web.entity.module;

import org.hswebframework.web.commons.entity.GenericEntity;

/**
 * 系统自定义模块 实体
 *
 * @author hsweb-generator-online
 */
public interface ModuleEntity extends GenericEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 名称
     */
    String name         = "name";
    /**
     * 权限id
     */
    String permissionId = "permissionId";
    /**
     * 备注
     */
    String remark       = "remark";
    /**
     * 列表配置
     */
    String listMeta     = "listMeta";
    /**
     * 保存页配置
     */
    String saveMeta     = "saveMeta";
    /**
     * 状态
     */
    String status       = "status";

    /**
     * @return 名称
     */
    String getName();

    /**
     * @param name 名称
     */
    void setName(String name);

    /**
     * @return 权限id
     */
    String getPermissionId();

    /**
     * @param permissionId 权限id
     */
    void setPermissionId(String permissionId);

    /**
     * @return 备注
     */
    String getRemark();

    /**
     * @param remark 备注
     */
    void setRemark(String remark);

    /**
     * @return 列表配置
     */
    String getListMeta();

    /**
     * @param listMeta 列表配置
     */
    void setListMeta(String listMeta);

    /**
     * @return 保存页配置
     */
    String getSaveMeta();

    /**
     * @param saveMeta 保存页配置
     */
    void setSaveMeta(String saveMeta);

    /**
     * @return 状态
     */
    Long getStatus();

    /**
     * @param status 状态
     */
    void setStatus(Long status);

}