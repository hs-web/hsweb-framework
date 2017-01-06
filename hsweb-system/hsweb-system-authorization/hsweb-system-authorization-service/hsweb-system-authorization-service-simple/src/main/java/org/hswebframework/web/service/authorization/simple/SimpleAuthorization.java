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

import org.hswebframework.web.entity.authorization.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
class SimpleAuthorization implements Authorization {
    private UserReadEntity                           userReadEntity;
    private List<PermissionRoleReadEntity>           permissionRoleReadEntities;
    private List<PermissionReadEntity<ActionEntity>> permissionReadEntities;

    public SimpleAuthorization(UserEntity user,
                               List<PermissionRoleEntity> permissionRoleEntities,
                               List<PermissionEntity<ActionEntity>> permissionReadEntities) {
        final String userId = user.getId();
        final String name = user.getName();
        final String userName = user.getUsername();
        final Date createDate = user.getCreateDate();
        final Date lastLoginDate = user.getLastLoginDate();
        final String lastLoginIp = user.getLastLoginIp();
        final boolean enabled = user.isEnabled();
        this.userReadEntity = new UserReadEntity() {
            @Override
            public String getId() {
                return userId;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getUsername() {
                return userName;
            }

            public Date getCreateDate() {
                return createDate;
            }

            public Date getLastLoginDate() {
                return lastLoginDate;
            }

            @Override
            public boolean isEnabled() {
                return enabled;
            }

            @Override
            public String getLastLoginIp() {
                return lastLoginIp;
            }
        };
        this.permissionRoleReadEntities = permissionRoleEntities.stream()
                .map(permissionRoleEntity ->
                        new PermissionRoleReadEntity() {
                            @Override
                            public String getRoleId() {
                                return permissionRoleEntity.getRoleId();
                            }

                            @Override
                            public String getPermissionId() {
                                return permissionRoleEntity.getPermissionId();
                            }

                            @Override
                            public List<String> getActions() {
                                return new ArrayList<>(permissionRoleEntity.getActions());
                            }
                        }
                ).collect(Collectors.toList());

        this.permissionReadEntities = permissionReadEntities.stream()
                .map(permission -> new PermissionReadEntity<ActionEntity>() {
                    @Override
                    public String getId() {
                        return permission.getId();
                    }

                    @Override
                    public String getName() {
                        return permission.getName();
                    }

                    @Override
                    public String getDescribe() {
                        return permission.getDescribe();
                    }

                    @Override
                    public byte getStatus() {
                        return permission.getStatus();
                    }

                    @Override
                    public List<ActionEntity> getActions() {
                        return new ArrayList<>(permission.getActions());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserReadEntity getUser() {
        return userReadEntity;
    }

    @Override
    public List<PermissionRoleReadEntity> getRoles() {
        return permissionRoleReadEntities;
    }

    @Override
    public List<PermissionReadEntity<ActionEntity>> getPermissions() {
        return permissionReadEntities;
    }

}
