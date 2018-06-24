package org.hswebframework.web.organizational.authorization.relation;

import java.util.List;

public interface RelationsManager {

    PersonRelations getPersonRelationsByPersonId(String personId);

    PersonRelations getPersonRelationsByUserId(String userId);

    DepartmentRelations getDepartmentRelations(List<String> departmentIds);

    OrgRelations getOrgRelations(List<String> orgIds);

    LinkedRelations getRelations(List<String> target);
}
