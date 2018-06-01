package org.hswebframework.web.entity.authorization.bind;

import org.hswebframework.web.entity.authorization.UserEntity;

import java.util.List;

/**
 * @author zhouhao
 */
public interface BindRoleUserEntity extends UserEntity {
    List<String> getRoles();

    void setRoles(List<String> roles);
}
