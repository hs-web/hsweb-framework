package org.hswebframework.web.service.organizational.simple.relations;

import org.hswebframework.web.entity.organizational.PersonEntity;
import org.hswebframework.web.organizational.authorization.relation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class DefaultRelationsManager implements RelationsManager {

    @Autowired
    private ServiceContext context;

    @Override
    public PersonRelations getPersonRelationsByPersonId(String personId) {
        return new DefaultPersonRelations(context, () -> Collections.singletonList(personId));
    }

    @Override
    public PersonRelations getPersonRelationsByUserId(String userId) {

        return new DefaultPersonRelations(context, () -> Optional
                .ofNullable(context.getPersonService().selectByUserId(userId))
                .map(PersonEntity::getId)
                .map(Collections::singletonList)
                .orElseGet(Collections::emptyList));
    }

    @Override
    public DepartmentRelations getDepartmentRelations(List<String> departmentIds) {
        return new DefaultDepartmentRelations(context, () -> departmentIds);
    }

    @Override
    public OrgRelations getOrgRelations(List<String> orgIds) {
        return new DefaultOrgRelations(context, () -> orgIds);
    }

    @Override
    public LinkedRelations getRelations(List<String> target) {

        return new DefaultLinkedRelations(context, () -> target);
    }
}
