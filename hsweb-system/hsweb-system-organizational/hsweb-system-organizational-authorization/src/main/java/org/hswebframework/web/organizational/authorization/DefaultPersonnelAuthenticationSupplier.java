package org.hswebframework.web.organizational.authorization;

import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.authorization.Authentication;

import java.util.Objects;

/**
 * 默认人员权限提供者,通过{@link PersonnelAuthenticationManager}获取,并提供ThreadLocal缓存
 *
 * @author zhouhao
 * @see 3.0
 */
public class DefaultPersonnelAuthenticationSupplier implements PersonnelAuthenticationSupplier {
    private PersonnelAuthenticationManager personnelAuthenticationManager;

    private static final String threadLocalCacheKey = DefaultPersonnelAuthenticationSupplier.class.getName() + "_CACHE";

    public DefaultPersonnelAuthenticationSupplier(PersonnelAuthenticationManager personnelAuthenticationManager) {
        Objects.requireNonNull(personnelAuthenticationManager);
        this.personnelAuthenticationManager = personnelAuthenticationManager;
    }

    @Override
    public PersonnelAuthentication getByPersonId(String personId) {
        return personnelAuthenticationManager.getPersonnelAuthorizationByPersonId(personId);
    }

    @Override
    public PersonnelAuthentication getByUserId(String userId) {
        return personnelAuthenticationManager.getPersonnelAuthorizationByUserId(userId);
    }

    @Override
    public PersonnelAuthentication get() {
        //TreadLocal Cache
        return ThreadLocalUtils.get(threadLocalCacheKey, () ->
                Authentication.current().map(authentication -> getByUserId(authentication.getUser().getId()))
                        .orElse(null));
    }
}
