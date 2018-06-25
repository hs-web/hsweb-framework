package org.hswebframework.web.service.organizational.simple.relations;

import org.hswebframework.web.organizational.authorization.relation.*;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DefaultPersonRelations extends DefaultLinkedRelations<PersonRelations> implements PersonRelations {


    public DefaultPersonRelations(ServiceContext serviceContext, Supplier<List<String>> personIdListSupplier) {
        super(serviceContext, personIdListSupplier);
    }

    protected List<String> getAllOrg() {
        return serviceContext
                .getPersonService()
                .selectAllOrgId(targetIdSupplier.get());
    }

    protected List<String> getAllDepartment() {
        return serviceContext
                .getPersonService()
                .selectAllDepartmentId(targetIdSupplier.get());
    }

    @Override
    public PersonRelations deep() {
        return new DefaultPersonRelations(serviceContext, () -> all()
                .stream()
                .map(Relation::getTarget)
                .collect(Collectors.toList()));
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
