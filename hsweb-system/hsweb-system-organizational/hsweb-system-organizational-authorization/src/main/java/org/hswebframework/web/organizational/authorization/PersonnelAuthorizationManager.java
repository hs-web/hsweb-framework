package org.hswebframework.web.organizational.authorization;

/**
 * 人员权限信息管理器
 *
 * @author zhouhao
 */
public interface PersonnelAuthorizationManager {
    PersonnelAuthorization getPersonnelAuthorizationByPersonId(String personId);

    PersonnelAuthorization getPersonnelAuthorizationByUserId(String userId);
}
