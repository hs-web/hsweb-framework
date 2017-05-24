package org.hswebframework.web.organizational.authorization.simple.handler;

import org.hsweb.ezorm.core.param.Term;
import org.hsweb.ezorm.core.param.TermType;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorization;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;
import org.hswebframework.web.organizational.authorization.entity.OrgAttachEntity;

import java.util.Collections;
import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class OrgScopeDataAccessHandler extends AbstractScopeDataAccessHander<OrgAttachEntity> {
    @Override
    protected Class<OrgAttachEntity> getEntityClass() {
        return OrgAttachEntity.class;
    }

    @Override
    protected String getSupportScope() {
        return DataAccessType.ORG_SCOPE;
    }

    @Override
    protected Set<String> getTryOperationScope(DataAccessType.ScopeType scopeType, PersonnelAuthorization authorization) {
        switch (scopeType) {
            case CHILDREN:
                return authorization.getAllOrgId();
            case ONLY_SELF:
                return authorization.getRootOrgId();
            default:
                return Collections.emptySet();
        }
    }

    @Override
    protected String getOperationScope(OrgAttachEntity entity) {
        return entity.getOrgId();
    }

    @Override
    protected Term applyQueryTerm(Set<String> scope) {
        Term term = new Term();
        term.setColumn(OrgAttachEntity.orgId);
        term.setTermType(TermType.in);
        term.setValue(scope);
        term.setType(Term.Type.and);
        return term;
    }
}
