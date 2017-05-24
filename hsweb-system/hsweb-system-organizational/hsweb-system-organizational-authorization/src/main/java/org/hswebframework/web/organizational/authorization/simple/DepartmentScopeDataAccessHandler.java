package org.hswebframework.web.organizational.authorization.simple;

import org.hsweb.ezorm.core.param.Term;
import org.hsweb.ezorm.core.param.TermType;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorization;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;
import org.hswebframework.web.organizational.authorization.entity.DepartmentAttachEntity;

import java.util.Collections;
import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DepartmentScopeDataAccessHandler extends AbstractScopeDataAccessHander<DepartmentAttachEntity> {
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
    protected Set<String> getTryOperationScope(DataAccessType.ScopeType scopeType, PersonnelAuthorization authorization) {
        switch (scopeType) {
            case CHILDREN:
                return authorization.getAllDepartmentId();
            case ONLY_SELF:
                return authorization.getRootDepartmentId();
            default:
                return Collections.emptySet();
        }
    }

    @Override
    protected Term applyQueryTerm(Set<String> scope) {
        Term term = new Term();
        term.setColumn(DepartmentAttachEntity.departmentId);
        term.setTermType(TermType.in);
        term.setValue(scope);
        term.setType(Term.Type.and);
        return term;
    }
}
