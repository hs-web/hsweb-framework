package org.hswebframework.web.organizational.authorization.simple.handler;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.organizational.authorization.access.OrgAttachEntity;
import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;

import java.util.Collections;
import java.util.Set;

import static org.hswebframework.web.organizational.authorization.access.DataAccessType.SCOPE_TYPE_CHILDREN;
import static org.hswebframework.web.organizational.authorization.access.DataAccessType.SCOPE_TYPE_ONLY_SELF;

/**
 * @author zhouhao
 */
public class OrgScopeDataAccessHandler extends AbstractScopeDataAccessHandler<OrgAttachEntity> {
    @Override
    protected Class<OrgAttachEntity> getEntityClass() {
        return OrgAttachEntity.class;
    }

    @Override
    protected String getSupportScope() {
        return DataAccessType.ORG_SCOPE;
    }

    @Override
    protected void applyScopeProperty(OrgAttachEntity entity, String value) {
        entity.setOrgId(value);
    }

    @Override
    protected Set<String> getTryOperationScope(String scopeType, PersonnelAuthentication authorization) {
        switch (scopeType) {
            case SCOPE_TYPE_CHILDREN:
                return authorization.getAllOrgId();
            case SCOPE_TYPE_ONLY_SELF:
                return authorization.getRootOrgId();
            default:
                return new java.util.HashSet<>();
        }
    }

    @Override
    protected String getOperationScope(OrgAttachEntity entity) {
        return entity.getOrgId();
    }

    @Override
    protected Term createQueryTerm(Set<String> scope, AuthorizingContext context) {
        Term term = new Term();
        term.setColumn(OrgAttachEntity.orgId);
        term.setTermType(TermType.in);
        term.setValue(scope);
        term.setType(Term.Type.and);
        return term;
    }
}
