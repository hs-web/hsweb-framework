package org.hswebframework.web.organizational.authorization.simple.handler;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.entity.organizational.OrganizationalEntity;
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
    protected Set<String> getTryOperationScope(String scopeType, PersonnelAuthorization authorization) {
        switch (scopeType) {
            case SCOPE_TYPE_CHILDREN:
                return authorization.getAllOrgId();
            case SCOPE_TYPE_ONLY_SELF:
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
    protected Term createQueryTerm(Set<String> scope, AuthorizingContext context) {
        Term term = new Term();
        if (genericTypeInstanceOf(OrganizationalEntity.class,context)) {
            term.setColumn(OrganizationalEntity.id);
        } else {
            term.setColumn(OrgAttachEntity.orgId);
        }
        term.setTermType(TermType.in);
        term.setValue(scope);
        term.setType(Term.Type.and);
        return term;
    }
}
