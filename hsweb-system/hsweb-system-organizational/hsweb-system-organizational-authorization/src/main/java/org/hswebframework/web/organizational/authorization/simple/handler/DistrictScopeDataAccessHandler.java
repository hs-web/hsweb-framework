package org.hswebframework.web.organizational.authorization.simple.handler;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.entity.organizational.authorization.DistrictAttachEntity;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorization;

import java.util.Collections;
import java.util.Set;

import static org.hswebframework.web.organizational.authorization.access.DataAccessType.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DistrictScopeDataAccessHandler extends AbstractScopeDataAccessHandler<DistrictAttachEntity> {
    @Override
    protected Class<DistrictAttachEntity> getEntityClass() {
        return DistrictAttachEntity.class;
    }

    @Override
    protected String getSupportScope() {
        return AREA_SCOPE;
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
    protected Set<String> getTryOperationScope(String scopeType, PersonnelAuthorization authorization) {
        switch (scopeType) {
            case SCOPE_TYPE_CHILDREN:
                return authorization.getAllDistrictId();
            case SCOPE_TYPE_ONLY_SELF:
                return authorization.getRootDistrictId();
            default:
                return Collections.emptySet();
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
