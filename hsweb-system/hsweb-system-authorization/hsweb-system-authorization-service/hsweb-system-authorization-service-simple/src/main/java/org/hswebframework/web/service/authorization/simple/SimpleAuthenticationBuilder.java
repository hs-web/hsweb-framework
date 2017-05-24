/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.service.authorization.simple;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.simple.*;
import org.hswebframework.web.entity.authorization.PermissionRoleEntity;
import org.hswebframework.web.entity.authorization.RoleEntity;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.service.authorization.DataAccessFactory;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleAuthenticationBuilder {

    public static Authentication build(UserEntity user,
                                       List<RoleEntity> roleEntities,
                                       List<PermissionRoleEntity> permissionRoleEntities,
                                       DataAccessFactory dataAccessFactory) {
        SimpleAuthentication authentication = new SimpleAuthentication();
        authentication.setUser(new SimpleUser(user.getId(), user.getUsername(), user.getName()));
        authentication.setRoles(roleEntities.stream()
                .map(roleEntity -> new SimpleRole(roleEntity.getId(), roleEntity.getName()))
                .collect(Collectors.toList()));
        authentication.setPermissions(permissionRoleEntities.stream()
                .map(permissionRoleEntity -> {
                    SimplePermission permission = new SimplePermission(permissionRoleEntity.getPermissionId(), new HashSet<>(permissionRoleEntity.getActions()));
                    if (null != dataAccessFactory && null != permissionRoleEntity.getDataAccesses()) {
                        permission.setDataAccesses(permissionRoleEntity
                                .getDataAccesses()
                                .stream()
                                .map(dataAccessFactory::create)
                                .collect(Collectors.toSet()));
                    }
                    if (null != permissionRoleEntity.getFieldAccesses()) {
                        permission.setFieldAccesses(permissionRoleEntity
                                .getFieldAccesses()
                                .stream()
                                .map(entity -> new SimpleFieldAccess(entity.getField(), new HashSet<>(entity.getActions())))
                                .collect(Collectors.toSet()));
                    }
                    return permission;
                })
                .collect(Collectors.toList()));
        return authentication;
    }

}
