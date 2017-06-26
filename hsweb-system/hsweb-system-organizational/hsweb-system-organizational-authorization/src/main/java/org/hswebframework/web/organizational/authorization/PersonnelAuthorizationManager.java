package org.hswebframework.web.organizational.authorization;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface PersonnelAuthorizationManager {
    PersonnelAuthorization getPersonnelAuthorizationByPersonId(String personId);

    PersonnelAuthorization getPersonnelAuthorizationByUserId(String userId);
}
