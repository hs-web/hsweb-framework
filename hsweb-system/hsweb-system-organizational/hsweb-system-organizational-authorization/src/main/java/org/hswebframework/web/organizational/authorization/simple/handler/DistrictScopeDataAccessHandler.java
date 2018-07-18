package org.hswebframework.web.organizational.authorization.simple.handler;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.organizational.authorization.access.DistrictAttachEntity;
import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;

import java.util.Collections;
import java.util.Set;

import static org.hswebframework.web.organizational.authorization.access.DataAccessType.*;

/**
 * @author zhouhao
 */
public class DistrictScopeDataAccessHandler extends AbstractScopeDataAccessHandler<DistrictAttachEntity> {
    @Override
    protected Class<DistrictAttachEntity> getEntityClass() {
        return DistrictAttachEntity.class;
    }

    @Override
    protected String getSupportScope() {
        return DISTRICT_SCOPE;
    }

    @Override
    protected String getOperationScope(DistrictAttachEntity entity) {
        return entity.getDistrictId();
    }

    @Override
    protected void applyScopeProperty(DistrictAttachEntity entity, String value) {
        entity.setDistrictId(value);
    }

    @Override
    protected Set<String> getTryOperationScope(String scopeType, PersonnelAuthentication authorization) {
        switch (scopeType) {
            case SCOPE_TYPE_CHILDREN:
                return authorization.getAllDistrictId();
            case SCOPE_TYPE_ONLY_SELF:
                return authorization.getRootDistrictId();
            default:
                return new java.util.HashSet<>();
        }
    }

    @Override
    protected Term createQueryTerm(Set<String> scope, AuthorizingContext context) {
        Term term = new Term();
        term.setColumn(DistrictAttachEntity.districtId);
        term.setTermType(TermType.in);
        term.setValue(scope);
        term.setType(Term.Type.and);
        return term;
    }
}
