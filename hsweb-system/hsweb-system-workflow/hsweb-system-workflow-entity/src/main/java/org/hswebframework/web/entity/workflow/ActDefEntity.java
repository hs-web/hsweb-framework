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
     * 矩阵ID
     */
    String defId = "defId";

    String getActId();

    void setActId(String actId);

    String getDefId();

    void setDefId(String defId);
}
