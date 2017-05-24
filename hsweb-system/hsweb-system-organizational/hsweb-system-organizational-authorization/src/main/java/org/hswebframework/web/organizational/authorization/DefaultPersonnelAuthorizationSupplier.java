package org.hswebframework.web.organizational.authorization;

import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationSupplier;
import org.hswebframework.web.organizational.authorization.entity.PersonAttachEntity;

import java.util.Objects;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DefaultPersonnelAuthorizationSupplier implements PersonnelAuthorizationSupplier {
    private PersonnelAuthorizationManager personnelAuthorizationManager;

    private static final String threadLocalCacheKey = DefaultPersonnelAuthorizationSupplier.class.getName() + "_CACHE";

    public DefaultPersonnelAuthorizationSupplier(PersonnelAuthorizationManager personnelAuthorizationManager) {
        this.personnelAuthorizationManager = personnelAuthorizationManager;
    }

    @Override
    public PersonnelAuthorization getByPersonId(String personId) {
        return personnelAuthorizationManager.getPersonnelAuthorizationByPersonId(personId);
    }

    @Override
    public PersonnelAuthorization getByUserId(String userId) {
        return personnelAuthorizationManager.getPersonnelAuthorizationByUserId(userId);
    }

    @Override
    public PersonnelAuthorization get() {
        //TreadLocal Cache
        return ThreadLocalUtils.get(threadLocalCacheKey, () ->
                Authentication.current().map(authentication ->
                        authentication.getAttribute(PersonAttachEntity.personId)
                                .filter(Objects::nonNull)
                                .map(String::valueOf)
                                .map(this::getByPersonId)
                                .orElseGet(() -> getByUserId(authentication.getUser().getId())))
                        .orElse(null));
    }
}
