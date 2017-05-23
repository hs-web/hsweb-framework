package organizational.simple;

import org.hsweb.ezorm.core.param.Term;
import org.hsweb.ezorm.core.param.TermType;
import organizational.PersonnelAuthorization;
import organizational.access.DataAccessType;
import organizational.entity.PersonAttachEntity;

import java.util.Collections;
import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class PersonScopeDataAccessHandler extends AbstractScopeDataAccessHander<PersonAttachEntity> {
    @Override
    protected Class<PersonAttachEntity> getEntityClass() {
        return PersonAttachEntity.class;
    }

    @Override
    protected String getSupportScope() {
        return DataAccessType.PERSON_SCOPE;
    }

    @Override
    protected Set<String> getTryOperationScope(DataAccessType.ScopeType scopeType, PersonnelAuthorization authorization) {
        switch (scopeType) {
            case CHILDREN:
                logger.warn("not support person children control!");
            case ONLY_SELF:
                return Collections.singleton(authorization.getPersonnel().getId());
            default:
                return Collections.emptySet();
        }
    }

    @Override
    protected String getOperationScope(PersonAttachEntity entity) {
        return entity.getPersonId();
    }

    @Override
    protected Term applyQueryTerm(Set<String> scope) {
        Term term = new Term();
        term.setColumn(PersonAttachEntity.personId);
        term.setTermType(TermType.in);
        term.setValue(scope);
        term.setType(Term.Type.and);
        return term;
    }
}
