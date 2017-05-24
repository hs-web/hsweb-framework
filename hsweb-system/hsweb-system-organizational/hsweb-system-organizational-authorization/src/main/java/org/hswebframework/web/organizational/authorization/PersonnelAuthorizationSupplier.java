package org.hswebframework.web.organizational.authorization;

import java.util.function.Supplier;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface PersonnelAuthorizationSupplier extends Supplier<PersonnelAuthorization> {
    PersonnelAuthorization getByPersonId(String personId);

    PersonnelAuthorization getByUserId(String userId);
}
