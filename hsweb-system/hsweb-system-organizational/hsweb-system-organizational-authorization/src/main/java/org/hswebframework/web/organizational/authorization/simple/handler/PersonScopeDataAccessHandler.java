package org.hswebframework.web.organizational.authorization.simple.handler;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.entity.organizational.authorization.PersonAttachEntity;
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
public class PersonScopeDataAccessHandler extends AbstractScopeDataAccessHandler<PersonAttachEntity> {
    @Override
    protected Class<PersonAttachEntity> getEntityClass() {
        return PersonAttachEntity.class;
    }

    @Override
    protected String getSupportScope() {
        return DataAccessType.PERSON_SCOPE;
    }

    @Override
    protected Set<String> getTryOperationScope(String scopeType, PersonnelAuthorization authorization) {
        switch (scopeType) {
            case SCOPE_TYPE_CHILDREN:
                logger.warn("not support person children control!");
            case SCOPE_TYPE_ONLY_SELF:
                return Collections.singleton(authorization.getPersonnel().getId());
            default:
                return Collections.emptySet();
        }
    }

    @Override
    protected void applyScopeProperty(PersonAttachEntity entity, String value) {
        entity.setPersonId(value);
    }

    @Override
    protected String getOperationScope(PersonAttachEntity entity) {
        return entity.getPersonId();
    }

    @Override
    protected Term createQueryTerm(Set<String> scope, AuthorizingContext context) {
        Term term = new Term();
        term.setColumn(PersonAttachEntity.personId);
        term.setTermType(TermType.in);
        term.setValue(scope);
        term.setType(Term.Type.and);
        return term;
    }
}
