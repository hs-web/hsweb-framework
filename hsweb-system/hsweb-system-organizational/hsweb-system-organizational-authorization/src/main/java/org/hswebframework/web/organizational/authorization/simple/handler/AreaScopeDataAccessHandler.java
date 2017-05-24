package org.hswebframework.web.organizational.authorization.simple.handler;

import org.hsweb.ezorm.core.param.Term;
import org.hsweb.ezorm.core.param.TermType;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorization;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;
import org.hswebframework.web.organizational.authorization.entity.AreaAttachEntity;

import java.util.Collections;
import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class AreaScopeDataAccessHandler extends AbstractScopeDataAccessHander<AreaAttachEntity> {
    @Override
    protected Class<AreaAttachEntity> getEntityClass() {
        return AreaAttachEntity.class;
    }

    @Override
    protected String getSupportScope() {
        return DataAccessType.DEPARTMENT_SCOPE;
    }

    @Override
    protected String getOperationScope(AreaAttachEntity entity) {
        return entity.getAreaId();
    }

    @Override
    protected Set<String> getTryOperationScope(DataAccessType.ScopeType scopeType, PersonnelAuthorization authorization) {
        switch (scopeType) {
            case CHILDREN:
                return authorization.getAllAreaId();
            case ONLY_SELF:
                return authorization.getRootAreaId();
            default:
                return Collections.emptySet();
        }
    }

    @Override
    protected Term applyQueryTerm(Set<String> scope) {
        Term term = new Term();
        term.setColumn(AreaAttachEntity.areaId);
        term.setTermType(TermType.in);
        term.setValue(scope);
        term.setType(Term.Type.and);
        return term;
    }
}
