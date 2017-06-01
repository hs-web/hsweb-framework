package org.hswebframework.web.organizational.authorization.simple.handler;

import org.hsweb.ezorm.core.param.Term;
import org.hsweb.ezorm.core.param.TermType;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorization;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;
import org.hswebframework.web.organizational.authorization.entity.DepartmentAttachEntity;

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
    protected Term createQueryTerm(Set<String> scope) {
        Term term = new Term();
        term.setColumn(DepartmentAttachEntity.departmentId);
        term.setTermType(TermType.in);
        term.setValue(scope);
        term.setType(Term.Type.and);
        return term;
    }
}
