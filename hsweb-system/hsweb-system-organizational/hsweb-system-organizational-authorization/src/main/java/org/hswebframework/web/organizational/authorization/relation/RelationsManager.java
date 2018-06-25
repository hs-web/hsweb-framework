package org.hswebframework.web.organizational.authorization.relation;

import java.util.List;

public interface RelationsManager {

    /**
     * 根据人员id获取人员的关系链
     * <pre>
     *     PersonRelations me = getPersonRelationsByPersonId(personId);
     *     me.department().relations("总监").all()
     * </pre>
     *
     * @param personId 人员id
     * @return 人员关系链
     */
    PersonRelations getPersonRelationsByPersonId(String personId);

    PersonRelations getPersonRelationsByUserId(String userId);

    DepartmentRelations getDepartmentRelations(List<String> departmentIds);

    OrgRelations getOrgRelations(List<String> orgIds);

    LinkedRelations getRelations(List<String> target);
}
