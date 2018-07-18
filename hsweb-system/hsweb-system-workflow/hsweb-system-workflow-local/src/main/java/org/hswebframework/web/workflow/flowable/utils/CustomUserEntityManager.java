package org.hswebframework.web.workflow.flowable.utils;

import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.hswebframework.web.entity.authorization.RoleEntity;
import org.hswebframework.web.service.authorization.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Collections;
import java.util.List;

public class CustomUserEntityManager extends UserEntityManager {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private UserService userService;

    public CustomUserEntityManager(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserEntity findUserById(final String userId) {
        if (userId == null) {
            return null;
        }
        org.hswebframework.web.entity.authorization.UserEntity user = userService.selectByPk(userId);
        return ActivitiUserUtil.toActivitiUser(user);
    }

    @Override
    public List<Group> findGroupsByUser(final String userId) {
        if (userId == null) {
            return new java.util.ArrayList<>();
        }

        List<RoleEntity> sysRoles = userService.getUserRole(userId);
        return ActivitiUserUtil.toActivitiGroups(sysRoles);

    }

    @Override
    public List<org.activiti.engine.identity.User> findUserByQueryCriteria(UserQueryImpl query, Page page) {
        throw new UnsupportedOperationException("not implement method..");
    }

    @Override
    public IdentityInfoEntity findUserInfoByUserIdAndKey(String userId,
                                                         String key) {
        throw new UnsupportedOperationException("not implement method.");
    }

    @Override
    public List<String> findUserInfoKeysByUserIdAndType(String userId,
                                                        String type) {
        throw new UnsupportedOperationException("not implement method.");
    }

    @Override
    public long findUserCountByQueryCriteria(UserQueryImpl query) {
        throw new UnsupportedOperationException("not implement method.");
    }
}
