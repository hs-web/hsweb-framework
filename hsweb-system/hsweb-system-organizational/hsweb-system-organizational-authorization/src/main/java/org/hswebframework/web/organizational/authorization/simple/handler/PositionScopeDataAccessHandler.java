package org.hswebframework.web.organizational.authorization.simple.handler;

import org.hsweb.ezorm.core.param.Term;
import org.hsweb.ezorm.core.param.TermType;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorization;
import org.hswebframework.web.organizational.authorization.entity.PositionAttachEntity;

import java.util.Collections;
import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class PositionScopeDataAccessHandler extends AbstractScopeDataAccessHander<PositionAttachEntity> {
    @Override
    protected Class<PositionAttachEntity> getEntityClass() {
        return PositionAttachEntity.class;
    }

    @Override
    protected String getSupportScope() {
        return DataAccessType.POSITION_SCOPE;
    }

    @Override
    protected Set<String> getTryOperationScope(DataAccessType.ScopeType scopeType, PersonnelAuthorization authorization) {
        switch (scopeType) {
            case CHILDREN:
                return authorization.getAllPositionId();
            case ONLY_SELF:
                return authorization.getRootPositionId();
            default:
                return Collections.emptySet();
        }
    }

    @Override
    protected String getOperationScope(PositionAttachEntity entity) {
        return entity.getPositionId();
    }

    @Override
    protected Term applyQueryTerm(Set<String> scope) {
        Term term = new Term();
        term.setColumn(PositionAttachEntity.positionId);
        term.setTermType(TermType.in);
        term.setValue(scope);
        term.setType(Term.Type.and);
        return term;
    }
}
