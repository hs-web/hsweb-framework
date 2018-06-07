package org.hswebframework.web.organizational.authorization;

/**
 * 人员权限信息管理器
 *
 * @author zhouhao
 */
public interface PersonnelAuthenticationManager {
    PersonnelAuthentication getPersonnelAuthorizationByPersonId(String personId);

    PersonnelAuthentication getPersonnelAuthorizationByUserId(String userId);
}
