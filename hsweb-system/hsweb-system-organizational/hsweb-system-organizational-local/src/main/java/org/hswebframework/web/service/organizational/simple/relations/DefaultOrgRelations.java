package org.hswebframework.web.service.organizational.simple.relations;

import org.hswebframework.web.entity.organizational.DepartmentEntity;
import org.hswebframework.web.organizational.authorization.relation.DepartmentRelations;
import org.hswebframework.web.organizational.authorization.relation.OrgRelations;
import org.hswebframework.web.organizational.authorization.relation.Relation;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DefaultOrgRelations extends DefaultLinkedRelations<OrgRelations> implements OrgRelations {

    private boolean includeChildren;

    private boolean includeParents;

    public DefaultOrgRelations(ServiceContext serviceContext, Supplier<List<String>> targetIdSupplier) {
        super(serviceContext, targetIdSupplier);
    }

    @Override
    public OrgRelations andChildren() {
        includeChildren = true;
        return this;
    }

    @Override
    public OrgRelations andParents() {
        includeParents = true;
        return this;
    }

    @Override
    public DepartmentRelations department() {
        return new DefaultDepartmentRelations(serviceContext, () -> serviceContext
                .getDepartmentService()
                .selectByOrgIds(targetIdSupplier.get(), includeChildren, includeParents)
                .stream()
                .map(DepartmentEntity::getId)
                .collect(Collectors.toList()));
    }

    @Override
    public OrgRelations deep() {
        return new DefaultOrgRelations(serviceContext, () -> stream().map(Relation::getTarget).collect(Collectors.toList()));
    }
}
