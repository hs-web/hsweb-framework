package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.Authorization;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.entity.authorization.bind.BindRoleUserEntity;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.service.CreateEntityService;
import org.hswebframework.web.service.QueryByEntityService;

import java.util.Date;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface UserService<Q extends Entity> extends
        CreateEntityService<UserEntity>,
        QueryByEntityService<UserEntity, Q> {

    String add(UserEntity userBean);

    boolean enable(String userId);

    boolean disable(String userId);

    void update(UserEntity userBean);

    UserEntity selectByUsername(String username);

    UserEntity selectById(String id);

    String encodePassword(String password, String salt);

    void updateLoginInfo(String userId, String ip, Date loginTime);

    void updatePassword(String userId, String oldPassword, String newPassword);

    Authorization initUserAuthorization(String userId);

    Authorization initAdminAuthorization(String userId);
}
