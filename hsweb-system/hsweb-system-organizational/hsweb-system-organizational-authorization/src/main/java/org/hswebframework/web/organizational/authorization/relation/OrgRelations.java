package org.hswebframework.web.organizational.authorization.relation;


public interface OrgRelations extends LinkedRelations<OrgRelations> {

    /**
     * 设置包含子级机构
     *
     * @return 机构关系链
     */
    OrgRelations andChildren();

    /**
     * 设置包含父级机构
     *
     * @return 机构关系链
     */
    OrgRelations andParents();

    /**
     * 获取全部的部门关系链
     *
     * @return 部门关系链
     */
    DepartmentRelations department();


}
