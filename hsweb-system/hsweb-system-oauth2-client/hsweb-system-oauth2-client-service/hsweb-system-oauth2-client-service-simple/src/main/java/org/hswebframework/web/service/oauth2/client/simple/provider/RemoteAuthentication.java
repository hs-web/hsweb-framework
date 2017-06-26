/*
 *  Copyright 2016 http://www.hswebframework.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.hswebframework.web.service.oauth2.client.simple.provider;

import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.Role;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.FieldAccessConfig;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
public class RemoteAuthentication implements Authentication {
    private ReadOnlyUser user;

    private List<ReadOnlyRole> roles;

    private List<ReadOnlyPermission> permissions;

    private Map<String, Serializable> attributes = new HashMap<>();

    public static RemoteAuthentication fromJson(String json) {
        RemoteAuthentication authentication = new RemoteAuthentication();

        JSONObject jsonObject = JSONObject.parseObject(json);

        authentication.setUser(jsonObject.getObject("user", ReadOnlyUser.class));
        authentication.setRoles(jsonObject.getJSONArray("roles").stream().map(role ->
                ((JSONObject) role).toJavaObject(ReadOnlyRole.class)
        ).collect(Collectors.toList()));
        authentication.setPermissions(jsonObject.getJSONArray("permissions").parallelStream().map(permission ->
                {
                    JSONObject permissionObj= ((JSONObject) permission);

                    return permissionObj.toJavaObject(ReadOnlyPermission.class);
                }
        ).collect(Collectors.toList()));
        authentication.setAttributes((Map) jsonObject.getJSONObject("attributes"));
        return authentication;
    }

    @Override
    public ReadOnlyUser getUser() {
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
    @SuppressWarnings("unchecked")
    public <T extends Serializable> Optional<T> getAttribute(String name) {
        return Optional.of((T) attributes.get(name));
    }

    @Override
    public void setAttribute(String name, Serializable value) {
        attributes.put(name, value);
    }

    public void setUser(ReadOnlyUser user) {
        checkWritable(this.user);
        this.user = user;
    }

    public void setRoles(List<ReadOnlyRole> roles) {
        checkWritable(this.roles);
        this.roles = roles;
    }

    public void setPermissions(List<ReadOnlyPermission> permissions) {
        checkWritable(this.permissions);
        this.permissions = permissions;
    }

    public void setAttributes(Map<String, Serializable> attributes) {
        this.attributes = attributes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T removeAttributes(String name) {
        return (T) attributes.remove(name);
    }

    public Map<String, Serializable> getAttributes() {
        return attributes;
    }

    public static class ReadOnlyPermission implements Permission {
        private String                 id;
        private Set<String>            actions;
        private Set<SimpleFieldAccess> fieldAccesses;
        private Set<DataAccessConfig>  dataAccessConfigs;

        public ReadOnlyPermission() {
        }

        public ReadOnlyPermission(String id, Collection<String> actions) {
            this.id = id;
            this.actions = new HashSet<>(actions);
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
        public Set<String> getActions() {
            if (actions == null) actions = Collections.emptySet();
            return new HashSet<>(actions);
        }

        @Override
        public Set<FieldAccessConfig> getFieldAccesses() {
            if (fieldAccesses == null) fieldAccesses = Collections.emptySet();
            return new HashSet<>(fieldAccesses);
        }

        public Set<DataAccessConfig> getDataAccessConfigs() {
            if (dataAccessConfigs == null) dataAccessConfigs = Collections.emptySet();
            return new HashSet<>(dataAccessConfigs);
        }

        public void setFieldAccesses(Set<SimpleFieldAccess> fieldAccesses) {
            checkWritable(this.fieldAccesses);
            this.fieldAccesses = fieldAccesses;
        }

        public void setDataAccessConfigs(Set<DataAccessConfig> dataAccessConfigs) {
            checkWritable(this.dataAccessConfigs);
            this.dataAccessConfigs = dataAccessConfigs;
        }

        public void setActions(Set<String> actions) {
            checkWritable(this.actions);
            this.actions = new HashSet<>(actions);
        }
    }

    public static class SimpleFieldAccess implements FieldAccessConfig {
        private String      field;
        private Set<String> actions;

        @Override
        public String getField() {
            return field;
        }

        @Override
        public Set<String> getActions() {
            return new HashSet<>(actions);
        }

        public void setField(String field) {
            checkWritable(this.field);
            this.field = field;
        }

        public void setActions(Set<String> actions) {
            checkWritable(this.actions);
            this.actions = actions;
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
