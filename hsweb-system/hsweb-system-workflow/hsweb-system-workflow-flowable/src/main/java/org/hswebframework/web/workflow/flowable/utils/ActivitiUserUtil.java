package org.hswebframework.web.workflow.flowable.utils;

import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.hswebframework.web.entity.authorization.RoleEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author wangwei
 * @Date 2017/8/3.
 */
public class ActivitiUserUtil {
    public static UserEntity toActivitiUser(org.hswebframework.web.entity.authorization.UserEntity bUser) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(bUser.getId());
        userEntity.setFirstName(bUser.getUsername());
        userEntity.setLastName(bUser.getName());
        userEntity.setPassword(bUser.getPassword());
//        userEntity.setEmail(bUser.getEmail());
        userEntity.setRevision(1);
        return userEntity;
    }

    public static GroupEntity toActivitiGroup(RoleEntity sysRole) {
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setId(sysRole.getId());
        groupEntity.setRevision(1);
        groupEntity.setType("assignment");
        groupEntity.setName(sysRole.getName());
        return groupEntity;
    }

    public static List<Group> toActivitiGroups(List<RoleEntity> roleEntities) {
        List<Group> groups = new ArrayList<Group>();
        for (RoleEntity roleEntity : roleEntities) {
            GroupEntity groupEntity = toActivitiGroup(roleEntity);
            groups.add(groupEntity);
        }
        return groups;
    }
}
