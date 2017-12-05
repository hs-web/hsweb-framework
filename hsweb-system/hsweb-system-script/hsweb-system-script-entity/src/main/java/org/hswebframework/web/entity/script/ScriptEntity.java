package org.hswebframework.web.entity.script;

import org.hswebframework.web.commons.entity.GenericEntity;

/**
 * 动态脚本 实体
 *
 * @author hsweb-generator-online
 */
public interface ScriptEntity extends GenericEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 脚本名称
     */
    String name     = "name";
    /**
     * 类型
     */
    String type     = "type";
    /**
     * 脚本内容
     */
    String script   = "script";
    /**
     * 脚本语言
     */
    String language = "language";
    /**
     * 备注
     */
    String remark   = "remark";
    /**
     * 状态
     */
    String status   = "status";
    /**
     * 脚本标签
     */
    String tag      = "tag";

    /**
     * @return 脚本名称
     */
    String getName();

    /**
     * @param name 脚本名称
     */
    void setName(String name);

    /**
     * @return 类型
     */
    String getType();

    /**
     * @param type 类型
     */
    void setType(String type);

    /**
     * @return 脚本内容
     */
    String getScript();

    /**
     * @param script 脚本内容
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
     * @return 备注
     */
    String getRemark();

    /**
     * @param remark 备注
     */
    void setRemark(String remark);

    /**
     * @return 状态
     */
    Long getStatus();

    /**
     * @param status 状态
     */
    void setStatus(Long status);

    /**
     * @return 脚本标签
     */
    String getTag();

    /**
     * @param tag 脚本标签
     */
    void setTag(String tag);

}