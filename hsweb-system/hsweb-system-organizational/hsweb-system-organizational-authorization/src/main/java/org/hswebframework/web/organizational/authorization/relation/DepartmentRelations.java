package org.hswebframework.web.organizational.authorization.relation;


public interface DepartmentRelations extends LinkedRelations<DepartmentRelations> {

    /**
     * 设置包含子级部门
     *
     * @return 部门关系链
     */
    DepartmentRelations andChildren();

    /**
     * 设置包含父级部门
     *
     * @return 部门关系链
     */
    DepartmentRelations andParents();

    /**
     * 获取部门下的人员关系链
     *
     * @return 人员关系链
     */
    PersonRelations persons();

}
