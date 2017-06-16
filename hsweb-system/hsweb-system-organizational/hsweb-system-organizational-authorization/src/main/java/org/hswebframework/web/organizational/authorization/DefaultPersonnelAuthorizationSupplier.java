package org.hswebframework.web.organizational.authorization;

import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.entity.organizational.authorization.PersonAttachEntity;

import java.util.Objects;

/**
 * 默认人员权限提供者,通过{@link PersonnelAuthorizationManager}获取,并提供ThreadLocal缓存
 *
 * @author zhouhao
 * @see 3.0
 */
public class DefaultPersonnelAuthorizationSupplier implements PersonnelAuthorizationSupplier {
    private PersonnelAuthorizationManager personnelAuthorizationManager;

    private static final String threadLocalCacheKey = DefaultPersonnelAuthorizationSupplier.class.getName() + "_CACHE";

    public DefaultPersonnelAuthorizationSupplier(PersonnelAuthorizationManager personnelAuthorizationManager) {
        Objects.requireNonNull(personnelAuthorizationManager);
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
        /*
            获取逻辑: 优先获取登录用户的权限信息中Authentication的personId属性;
            如果不存在,则根据用户id获取.如果还不存在则返回null
         */
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
