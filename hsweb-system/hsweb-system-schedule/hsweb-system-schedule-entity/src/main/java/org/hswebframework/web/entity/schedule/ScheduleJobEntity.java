package org.hswebframework.web.entity.schedule;

import org.hswebframework.web.commons.entity.GenericEntity;

/**
 * 调度任务 实体
 *
 * @author hsweb-generator-online
 */
public interface ScheduleJobEntity extends GenericEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 任务名称
     */
    String name         = "name";
    /**
     * 备注
     */
    String remark       = "remark";
    /**
     * 定时调度配置
     */
    String quartzConfig = "quartzConfig";
    /**
     * 执行脚本
     */
    String script       = "script";
    /**
     * 脚本语言
     */
    String language     = "language";
    /**
     * 是否启用
     */
    String status       = "status";
    /**
     * 启动参数
     */
    String parameters   = "parameters";
    /**
     * 任务类型
     */
    String type         = "type";
    /**
     * 标签
     */
    String tags         = "tags";

    /**
     * @return 任务名称
     */
    String getName();

    /**
     * @param name 任务名称
     */
    void setName(String name);

    /**
     * @return 备注
     */
    String getRemark();

    /**
     * @param remark 备注
     */
    void setRemark(String remark);

    /**
     * @return 定时调度配置
     */
    String getQuartzConfig();

    /**
     * @param quartzConfig 定时调度配置
     */
    void setQuartzConfig(String quartzConfig);

    /**
     * @return 执行脚本
     */
    String getScript();

    /**
     * @param script 执行脚本
     */
    void setScript(String script);

    /**
     * @return 脚本语言
     */
    String getLanguage();

    /**
     * @param language 脚本语言
     */
    void setLanguage(String language);

    /**
     * @return 是否启用
     */
    Byte getStatus();

    /**
     * @param status 是否启用
     */
    void setStatus(Byte status);

    /**
     * @return 启动参数
     */
    String getParameters();

    /**
     * @param parameters 启动参数
     */
    void setParameters(String parameters);

    /**
     * @return 任务类型
     */
    String getType();

    /**
     * @param type 任务类型
     */
    void setType(String type);

    /**
     * @return 标签
     */
    String getTags();

    /**
     * @param tags 标签
     */
    void setTags(String tags);

}