package org.hswebframework.web.dao.authorization;

import org.hswebframework.web.dao.Dao;
import org.hswebframework.web.entity.authorization.UserRoleEntity;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface UserRoleDao extends Dao {
    int deleteByUserId(String userId);

    int deleteByRoleId(String roleId);

    void insert(UserRoleEntity userRoleBean);

    List<UserRoleEntity> selectByUserId(String userId);

    List<UserRoleEntity> selectByRoleId(String roleId);
}
