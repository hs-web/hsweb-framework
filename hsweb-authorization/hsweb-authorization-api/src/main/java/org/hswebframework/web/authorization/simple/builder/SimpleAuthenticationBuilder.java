package org.hswebframework.web.authorization.simple.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.Role;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.builder.AuthenticationBuilder;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.authorization.simple.SimplePermission;
import org.hswebframework.web.authorization.simple.SimpleRole;
import org.hswebframework.web.authorization.simple.SimpleUser;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleAuthenticationBuilder implements AuthenticationBuilder {
    private SimpleAuthentication authentication = new SimpleAuthentication();

    private DataAccessConfigBuilderFactory dataBuilderFactory;

    public SimpleAuthenticationBuilder(DataAccessConfigBuilderFactory dataBuilderFactory) {
        this.dataBuilderFactory = dataBuilderFactory;
    }

    public void setDataBuilderFactory(DataAccessConfigBuilderFactory dataBuilderFactory) {
        this.dataBuilderFactory = dataBuilderFactory;
    }

    @Override
    public AuthenticationBuilder user(User user) {
        Objects.requireNonNull(user);
        authentication.setUser(user);
        return this;
    }

    @Override
    public AuthenticationBuilder user(String user) {
        return user(JSON.parseObject(user, SimpleUser.class));
    }

    @Override
    public AuthenticationBuilder user(Map<String, String> user) {
        Objects.requireNonNull(user.get("id"));
        user(new SimpleUser(user.get("id"), user.get("username"), user.get("name")));
        return this;
    }

    @Override
    public AuthenticationBuilder role(List<Role> role) {
        authentication.setRoles(role);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuthenticationBuilder role(String role) {
        return role((List) JSON.parseArray(role, SimpleRole.class));
    }

    @Override
    public AuthenticationBuilder permission(List<Permission> permission) {
        authentication.setPermissions(permission);
        return this;
    }

    @Override
    public AuthenticationBuilder permission(String permissionJson) {
        JSONArray jsonArray = JSON.parseArray(permissionJson);
        List<Permission> permissions = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            SimplePermission permission = new SimplePermission();
            permission.setId(jsonObject.getString("id"));
            permission.setActions(new HashSet<>(jsonObject.getJSONArray("actions").toJavaList(String.class)));
            permission.setDataAccesses(jsonObject.getJSONArray("dataAccesses").stream().map(JSONObject.class::cast)
                    .map(dataJson -> dataBuilderFactory.create().fromJson(dataJson.toJSONString()).build())
                    .collect(Collectors.toSet()));
            permissions.add(permission);
        }
        authentication.setPermissions(permissions);
        return this;
    }

    @Override
    public AuthenticationBuilder attributes(String attributes) {
        authentication.setAttributes(JSON.<Map<String, Serializable>>parseObject(attributes, Map.class));
        return this;
    }

    @Override
    public AuthenticationBuilder attributes(Map<String, Serializable> permission) {
        authentication.setAttributes(permission);
        return this;
    }

    @Override
    public AuthenticationBuilder json(String json) {
        JSONObject jsonObject = JSON.parseObject(json);
        user(jsonObject.getObject("user", SimpleUser.class));
        role(jsonObject.getJSONArray("roles").toJSONString());
        permission(jsonObject.getJSONArray("permissions").toJSONString());
        return this;
    }

    @Override
    public Authentication build() {
        return authentication;
    }
}
