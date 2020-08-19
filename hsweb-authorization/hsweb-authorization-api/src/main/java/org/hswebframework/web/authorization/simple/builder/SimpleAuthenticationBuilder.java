package org.hswebframework.web.authorization.simple.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.authorization.*;
import org.hswebframework.web.authorization.builder.AuthenticationBuilder;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.simple.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
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
        user(SimpleUser.builder()
                .id(user.get("id"))
                .username(user.get("username"))
                .name(user.get("name"))
                .userType(user.get("type"))
                .build());
        return this;
    }

    @Override
    public AuthenticationBuilder role(List<Role> role) {
        authentication.getDimensions().addAll(role);
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
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            SimplePermission permission = new SimplePermission();
            permission.setId(jsonObject.getString("id"));
            permission.setName(jsonObject.getString("name"));
            permission.setOptions(jsonObject.getJSONObject("options"));
            JSONArray actions = jsonObject.getJSONArray("actions");
            if (actions != null) {
                permission.setActions(new HashSet<>(actions.toJavaList(String.class)));
            }
            JSONArray dataAccess = jsonObject.getJSONArray("dataAccesses");
            if (null != dataAccess) {
                permission.setDataAccesses(dataAccess.stream().map(JSONObject.class::cast)
                        .map(dataJson -> dataBuilderFactory.create().fromJson(dataJson.toJSONString()).build())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));
            }
            permissions.add(permission);
        }
        authentication.setPermissions(permissions);
        return this;
    }

    @Override
    public AuthenticationBuilder attributes(String attributes) {
        authentication.getAttributes().putAll(JSON.<Map<String, Serializable>>parseObject(attributes, Map.class));
        return this;
    }

    @Override
    public AuthenticationBuilder attributes(Map<String, Serializable> permission) {
        authentication.getAttributes().putAll(permission);
        return this;
    }

    public AuthenticationBuilder dimension(JSONArray json) {

        if (json == null) {
            return this;
        }
        List<Dimension> dimensions = new ArrayList<>();

        for (int i = 0; i < json.size(); i++) {
            JSONObject jsonObject = json.getJSONObject(i);
            Object type = jsonObject.get("type");

            dimensions.add( SimpleDimension.of(
                    jsonObject.getString("id"),
                    jsonObject.getString("name"),
                    type instanceof String?SimpleDimensionType.of(String.valueOf(type)):jsonObject.getJSONObject("type").toJavaObject(SimpleDimensionType.class),
                    jsonObject.getJSONObject("options")
            ));
        }
        authentication.setDimensions(dimensions);

        return this;

    }

    @Override
    public AuthenticationBuilder json(String json) {
        JSONObject jsonObject = JSON.parseObject(json);
        user(jsonObject.getObject("user", SimpleUser.class));
        if (jsonObject.containsKey("roles")) {
            role(jsonObject.getJSONArray("roles").toJSONString());
        }
        if (jsonObject.containsKey("permissions")) {
            permission(jsonObject.getJSONArray("permissions").toJSONString());
        }
        if (jsonObject.containsKey("dimensions")) {
            dimension(jsonObject.getJSONArray("dimensions"));
        }
        return this;
    }

    @Override
    public Authentication build() {
        return authentication;
    }
}
