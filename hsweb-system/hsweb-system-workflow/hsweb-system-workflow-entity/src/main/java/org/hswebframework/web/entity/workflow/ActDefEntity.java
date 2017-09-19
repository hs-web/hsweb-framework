package org.hswebframework.web.entity.workflow;

import org.hswebframework.web.commons.entity.GenericEntity;

/**
 * @Author wangwei
 * @Date 2017/9/5.
 */
public interface ActDefEntity extends GenericEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 节点ID
     */
    String actId = "actId";
    /**
     * 表单ID
     */
    String formId = "formId";
    /**
     * 关系ID
     */
    String defId = "defId";

    /**
     * 关系类型
     */
    String type = "type";

    String getActId();

    void setActId(String actId);

    String getFormId();

    void setFormId(String formId);

    String getDefId();

    void setDefId(String defId);

    String getType();

    void setType(String type);
}
