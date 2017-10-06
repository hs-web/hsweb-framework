package org.hswebframework.web.organizational.authorization.simple.handler;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.entity.organizational.DepartmentEntity;
import org.hswebframework.web.entity.organizational.OrganizationalEntity;
import org.hswebframework.web.entity.organizational.authorization.DepartmentAttachEntity;
import org.hswebframework.web.entity.organizational.authorization.OrgAttachEntity;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorization;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;

import java.util.Collections;
import java.util.Set;

import static org.hswebframework.web.organizational.authorization.access.DataAccessType.SCOPE_TYPE_CHILDREN;
import static org.hswebframework.web.organizational.authorization.access.DataAccessType.SCOPE_TYPE_ONLY_SELF;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DepartmentScopeDataAccessHandler extends AbstractScopeDataAccessHandler<DepartmentAttachEntity> {
    @Override
    protected Class<DepartmentAttachEntity> getEntityClass() {
        return DepartmentAttachEntity.class;
    }

    @Override
    protected void applyScopeProperty(DepartmentAttachEntity entity, String value) {
        entity.setDepartmentId(value);
    }

    @Override
    protected String getSupportScope() {
        return DataAccessType.DEPARTMENT_SCOPE;
    }

    @Override
    protected String getOperationScope(DepartmentAttachEntity entity) {
        return entity.getDepartmentId();
    }

    @Override
    protected Set<String> getTryOperationScope(String scopeType, PersonnelAuthorization authorization) {
        switch (scopeType) {
            case SCOPE_TYPE_CHILDREN:
                return authorization.getAllDepartmentId();
            case SCOPE_TYPE_ONLY_SELF:
                return authorization.getRootDepartmentId();
            default:
                return Collections.emptySet();
        }
    }

    @Override
    protected Term createQueryTerm(Set<String> scope, AuthorizingContext context) {
        Term term = new Term();
        if (genericTypeInstanceOf(DepartmentEntity.class,context)) {
            term.setColumn(DepartmentEntity.id);
        } else {
            term.setColumn(DepartmentAttachEntity.departmentId);
        }
        term.setTermType(TermType.in);
        term.setValue(scope);
        term.setType(Term.Type.and);
        return term;
    }
}
