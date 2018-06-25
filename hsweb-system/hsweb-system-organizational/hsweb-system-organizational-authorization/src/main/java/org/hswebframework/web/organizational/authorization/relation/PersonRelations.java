package org.hswebframework.web.organizational.authorization.relation;

public interface PersonRelations extends LinkedRelations<PersonRelations> {

    /**
     * @return 人员所在部门关系链
     */
    DepartmentRelations department();

    /**
     * @return 人员所在机构关系链
     */
    OrgRelations org();

}
