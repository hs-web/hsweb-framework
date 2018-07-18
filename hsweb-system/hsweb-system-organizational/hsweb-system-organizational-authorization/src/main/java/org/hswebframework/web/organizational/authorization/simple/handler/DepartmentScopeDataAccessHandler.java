package org.hswebframework.web.organizational.authorization.simple.handler;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.organizational.authorization.access.DepartmentAttachEntity;
import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;

import java.util.Collections;
import java.util.Set;

import static org.hswebframework.web.organizational.authorization.access.DataAccessType.SCOPE_TYPE_CHILDREN;
import static org.hswebframework.web.organizational.authorization.access.DataAccessType.SCOPE_TYPE_ONLY_SELF;

/**
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
    protected Set<String> getTryOperationScope(String scopeType, PersonnelAuthentication authorization) {
        switch (scopeType) {
            case SCOPE_TYPE_CHILDREN:
                return authorization.getAllDepartmentId();
            case SCOPE_TYPE_ONLY_SELF:
                return authorization.getRootDepartmentId();
            default:
                return new java.util.HashSet<>();
        }
    }

    @Override
    protected Term createQueryTerm(Set<String> scope, AuthorizingContext context) {
        Term term = new Term();
        term.setColumn(DepartmentAttachEntity.departmentId);
        term.setTermType(TermType.in);
        term.setValue(scope);
        term.setType(Term.Type.and);
        return term;
    }
}
