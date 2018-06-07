package org.hswebframework.web.workflow.flowable.utils;

import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.hswebframework.web.entity.authorization.RoleEntity;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.ArrayList;
import java.util.List;

public class CustomGroupEntityManager extends GroupEntityManager {

    private UserService userService;

    public CustomGroupEntityManager(UserService userService) {
        this.userService = userService;
    }

    public GroupEntity findGroupById(final String id) {
        if (id == null) {
            return null;
        }
        try {
            List<RoleEntity> sysRoles = userService.getUserRole(id);
            return ActivitiUserUtil.toActivitiGroup(sysRoles.get(0));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Group> findGroupsByUser(final String id) {
        if (id == null) {
            return null;
        }
        try {
            List<RoleEntity> sysRoles = userService.getUserRole(id);
            return ActivitiUserUtil.toActivitiGroups(sysRoles);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
        throw new RuntimeException("not implement method.");
    }

    @Override
    public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
        throw new RuntimeException("not implement method.");
    }
}
