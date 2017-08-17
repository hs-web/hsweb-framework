package org.hswebframework.web.organizational.authorization.access;

import java.io.Serializable;

/**
 * 控制类型
 *
 * @author zhouhao
 * @since 3.0
 */
public interface DataAccessType extends Serializable {
    /**
     * 控制地区
     */
    String AREA_SCOPE           = "AREA_SCOPE";
    /**
     * 控制机构
     */
    String ORG_SCOPE            = "ORG_SCOPE";
    /**
     * 控制部门
     */
    String DEPARTMENT_SCOPE     = "DEPARTMENT_SCOPE";
    /**
     * 控制职位
     */
    String POSITION_SCOPE       = "POSITION_SCOPE";
    /**
     * 控制人员
     */
    String PERSON_SCOPE         = "PERSON_SCOPE";
    /**
     * 仅限本人
     */
    String SCOPE_TYPE_ONLY_SELF = "ONLY_SELF";
    /**
     * 包含子级
     */
    String SCOPE_TYPE_CHILDREN  = "CHILDREN";
    /**
     * 自定义范围
     */
    String SCOPE_TYPE_CUSTOM    = "CUSTOM_SCOPE";

}
