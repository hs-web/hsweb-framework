package org.hswebframework.web.entity.workflow;

import org.hswebframework.web.commons.entity.GenericEntity;

/**
 * @Author wangwei
 * @Date 2017/9/5.
 */
public interface ProcDefFormEntity extends GenericEntity<String>{
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 流程定义Key
     */
    String defKey = "defKey";
    /**
     * 动态表单ID
     */
    String formId = "formId";

    String getDefKey();

    void setDefKey(String defKey);

    String getFormId();

    void setFormId(String formId);
}
