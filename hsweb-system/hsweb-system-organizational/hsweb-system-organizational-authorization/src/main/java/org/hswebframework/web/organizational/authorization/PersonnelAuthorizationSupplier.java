package org.hswebframework.web.organizational.authorization;

import java.util.function.Supplier;

/**
 * 人员权限提供者,用于根据人员或者用户id获取权限信息
 *
 * @author zhouhao
 * @see 3.0
 */
public interface PersonnelAuthorizationSupplier extends Supplier<PersonnelAuthorization> {
    PersonnelAuthorization getByPersonId(String personId);

    PersonnelAuthorization getByUserId(String userId);
}
