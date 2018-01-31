package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.RoleEntity;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.service.CreateEntityService;
import org.hswebframework.web.service.InsertService;
import org.hswebframework.web.service.QueryByEntityService;
import org.hswebframework.web.service.QueryService;

import java.util.List;

/**
 * 用户服务
 *
 * @author zhouhao
 * @since 3.0
 */
public interface UserService extends
        CreateEntityService<UserEntity>,
        QueryByEntityService<UserEntity>,
        QueryService<UserEntity, String>,
        InsertService<UserEntity, String> {

    boolean enable(String userId);

    boolean disable(String userId);

    void update(String userId, UserEntity userBean);

    UserEntity selectByUsername(String username);

    UserEntity selectByUserNameAndPassword(String plainUsername,String plainPassword);

    String encodePassword(String password, String salt);

    void updatePassword(String userId, String oldPassword, String newPassword);

    List<RoleEntity> getUserRole(String userId);
}
