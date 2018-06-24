package org.hswebframework.web.service.organizational.simple.relations;

import org.hswebframework.web.organizational.authorization.relation.*;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.service.organizational.PersonService;
import org.hswebframework.web.service.organizational.RelationInfoService;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class DefaultPersonRelations extends DefaultLinkedRelations<PersonRelations> implements PersonRelations {


    public DefaultPersonRelations(ServiceContext serviceContext, Supplier<List<String>> personIdListSupplier) {
        super(serviceContext, personIdListSupplier);
    }

    protected List<String> getAllOrg() {
        return serviceContext.getPersonService().selectAllOrgId(targetIdSupplier.get());
    }

    protected List<String> getAllDepartment() {
        return serviceContext.getPersonService().selectAllDepartmentId(targetIdSupplier.get());
    }

    @Override
    public DepartmentRelations department() {
        return new DefaultDepartmentRelations(serviceContext, createLazyIdSupplier(this::getAllDepartment));
    }

    @Override
    public OrgRelations org() {
        return new DefaultOrgRelations(serviceContext, createLazyIdSupplier(this::getAllOrg));
    }

}
