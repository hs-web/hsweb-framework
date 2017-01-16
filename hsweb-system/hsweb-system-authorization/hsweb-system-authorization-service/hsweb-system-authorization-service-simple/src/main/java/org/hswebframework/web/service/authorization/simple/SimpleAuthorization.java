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

import org.hswebframework.web.authorization.Authorization;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.Role;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.entity.authorization.*;

import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleAuthorization implements Authorization {
    private ReadOnlyUser user;

    private List<Role> roles;

    private List<Permission> permissions;

    private Map<String, Serializable> attributes = new HashMap<>();

    public SimpleAuthorization() {
    }

    public SimpleAuthorization(UserEntity user,
                               List<RoleEntity> roleEntities,
                               List<PermissionRoleEntity> permissionRoleEntities) {
        this.user = new ReadOnlyUser(user.getId(), user.getUsername(), user.getName());
        this.roles = roleEntities.stream()
                .map(roleEntity -> new ReadOnlyRole(roleEntity.getId(), roleEntity.getDescribe()))
                .collect(Collectors.toList());
        this.permissions = permissionRoleEntities.stream()
                .map(permissionRoleEntity -> new ReadOnlyPermission(permissionRoleEntity.getPermissionId(), permissionRoleEntity.getActions()))
                .collect(Collectors.toList());
    }


    @Override
    public User getUser() {
        return user;
    }

    @Override
    public List<Role> getRoles() {
        return new ArrayList<>(roles);
    }

    @Override
    public List<Permission> getPermissions() {
        return new ArrayList<>(permissions);
    }

    @Override
    public <T extends Serializable> T getAttribute(String name) {
        return ((T) attributes.get(name));
    }

    @Override
    public <T extends Serializable> T getAttribute(String name, T defaultValue) {
        T val = getAttribute(name);
        return val == null ? defaultValue : val;
    }

    @Override
    public <T extends Serializable> T getAttribute(String name, Supplier<T> supplier) {
        T val = getAttribute(name);
        return val == null ? supplier.get() : val;
    }

    @Override
    public void setAttribute(String name, Serializable value) {
        attributes.put(name, value);
    }

    public void setUser(ReadOnlyUser user) {
        checkWritable(this.user);
        this.user = user;
    }

    public void setRoles(List<Role> roles) {
        checkWritable(this.roles);
        this.roles = roles;
    }

    public void setPermissions(List<Permission> permissions) {
        checkWritable(this.permissions);
        this.permissions = permissions;
    }

    public void setAttributes(Map<String, Serializable> attributes) {
        this.attributes = attributes;
    }


    public static class ReadOnlyPermission implements Permission {
        private String       id;
        private List<String> actions;

        public ReadOnlyPermission() {
        }

        public ReadOnlyPermission(String id, List<String> actions) {
            this.id = id;
            this.actions = actions;
        }

        @Override
        public String getId() {
            return id;
        }

        public void setId(String id) {
            checkWritable(this.id);
            this.id = id;
        }

        @Override
        public List<String> getActions() {
            return new ArrayList<>(actions);
        }

        public void setActions(List<String> actions) {
            checkWritable(this.actions);
            this.actions = new ArrayList<>(actions);
        }
    }

    public static class ReadOnlyRole implements Role {
        private String id;

        private String name;

        public ReadOnlyRole() {
        }

        public ReadOnlyRole(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setId(String id) {
            checkWritable(this.id);
            this.id = id;
        }

        public void setName(String name) {
            checkWritable(this.name);
            this.name = name;
        }
    }

    public static class ReadOnlyUser implements User {
        private String id;

        private String username;

        private String name;

        public ReadOnlyUser() {
        }

        public ReadOnlyUser(String id, String username, String name) {
            this.id = id;
            this.username = username;
            this.name = name;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setId(String id) {
            checkWritable(this.id);
            this.id = id;
        }

        public void setUsername(String username) {
            checkWritable(this.username);
            this.username = username;
        }

        public void setName(String name) {
            checkWritable(this.name);
            this.name = name;
        }
    }

    static final void checkWritable(Object obj) {
        if (obj != null) {
            throw new UnsupportedOperationException();
        }
    }
}
