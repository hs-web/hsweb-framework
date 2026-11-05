package org.hswebframework.web.authorization.simple.builder;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Maps;
import org.hswebframework.web.authorization.*;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.builder.AuthenticationBuilder;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.simple.*;
import org.jetlinks.json.Json;
import org.jetlinks.json.JsonArray;
import org.jetlinks.json.JsonMapper;
import org.jetlinks.json.JsonType;

import java.io.Serializable;
import java.util.*;

/**
 * 从用户、角色及权限构建认证信息。默认使用 facade 应用配置，可通过构造参数替换。
 * 构建器保存当前认证状态，应按次创建；中立数组入口直接传递数据权限 Map。
 *
 * @author zhouhao
 */
public class SimpleAuthenticationBuilder implements AuthenticationBuilder {
    // 角色类型在 String 读取和完整文档转换中复用，避免每次构造集合类型。
    private static final JsonType<List<SimpleRole>> ROLES = JsonType.listOf(JsonType.of(SimpleRole.class));

    private final JsonMapper mapper;
    private SimpleAuthentication authentication = new SimpleAuthentication();

    private DataAccessConfigBuilderFactory dataBuilderFactory;

    public SimpleAuthenticationBuilder(DataAccessConfigBuilderFactory dataBuilderFactory) {
        this(dataBuilderFactory, Json.global());
    }

    /** 使用稳定 JSON 配置读取认证数据；构建器本身不提供并发同步。 */
    public SimpleAuthenticationBuilder(DataAccessConfigBuilderFactory dataBuilderFactory, JsonMapper mapper) {
        this.dataBuilderFactory = dataBuilderFactory;
        this.mapper = Objects.requireNonNull(mapper, "mapper");
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
        return user(mapper.read(user, SimpleUser.class));
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
        authentication.addDimensions(role);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuthenticationBuilder role(String role) {
        return role((List) mapper.read(role, ROLES));
    }

    @Override
    public AuthenticationBuilder permission(List<Permission> permission) {
        authentication.addPermissions(permission);
        return this;
    }

    /** @deprecated 使用 {@link #permissionJson(JsonArray)}，不再绑定 fastjson 容器。 */
    @Deprecated
    public AuthenticationBuilder permission(JSONArray jsonArray) {
        return permissionValues(jsonArray);
    }

    /** 从中立数组构建权限，已有数据权限对象直接通过 fromMap 传递。 */
    public AuthenticationBuilder permissionJson(JsonArray jsonArray) {
        return permissionValues(jsonArray);
    }

    // 两种公开容器共用 List/Map 路径，保留旧 options 的可变性和引用，不创建只读中转视图。
    @SuppressWarnings("unchecked")
    private AuthenticationBuilder permissionValues(List<?> values) {
        List<Permission> permissions = new ArrayList<>();
        for (Object value : values) {
            Map<String, Object> jsonObject = (Map<String, Object>) value;
            SimplePermission permission = new SimplePermission();
            permission.setId(Objects.toString(jsonObject.get("id"), null));
            permission.setName(Objects.toString(jsonObject.get("name"), null));
            permission.setOptions((Map<String, Object>) jsonObject.get("options"));
            Collection<?> actions = (Collection<?>) jsonObject.get("actions");
            if (actions != null) {
                Set<String> actionSet = new HashSet<>();
                for (Object action : actions) {
                    actionSet.add(Objects.toString(action, null));
                }
                permission.setActions(actionSet);
            }
            List<?> dataAccess = (List<?>) jsonObject.get("dataAccesses");
            if (null != dataAccess) {
                Set<DataAccessConfig> configs = new HashSet<>();
                for (Object access : dataAccess) {
                    var config = dataBuilderFactory.create().fromMap((Map<String, Object>) access).build();
                    if (config != null) {
                        configs.add(config);
                    }
                }
                permission.setDataAccesses(configs);
            }
            permissions.add(permission);
        }
        authentication.addPermissions(permissions);
        return this;
    }

    @Override
    public AuthenticationBuilder permission(String permissionJson) {
        // 保留旧数组入口的覆写分派，仅包装已有 List，不复制或重新编码。
        return permission(new JSONArray(mapper.readAsList(permissionJson, Object.class)));
    }

    @Override
    public AuthenticationBuilder attributes(String attributes) {
        authentication.putAttributes(mapper.readAsMap(attributes, Serializable.class));
        return this;
    }

    @Override
    public AuthenticationBuilder attributes(Map<String, Serializable> permission) {
        authentication.putAttributes(permission);
        return this;
    }

    /** @deprecated 使用 {@link #dimensionJson(JsonArray)}，保留旧容器入口。 */
    @Deprecated
    public AuthenticationBuilder dimension(JSONArray json) {
        return dimensionValues(json);
    }

    /** 读取中立维度数组；type 支持字符串 ID 或维度类型对象。 */
    public AuthenticationBuilder dimensionJson(JsonArray json) {
        return dimensionValues(json);
    }

    @SuppressWarnings("unchecked")
    private AuthenticationBuilder dimensionValues(List<?> json) {
        if (json == null) {
            return this;
        }
        List<Dimension> dimensions = new ArrayList<>();

        for (Object value : json) {
            Map<String, Object> jsonObject = (Map<String, Object>) value;
            Object type = jsonObject.get("type");
            Map<String, Object> options = (Map<String, Object>) jsonObject.get("options");

            dimensions.add(SimpleDimension.of(
                Objects.toString(jsonObject.get("id"), null),
                Objects.toString(jsonObject.get("name"), null),
                type instanceof String ? SimpleDimensionType.of((String) type) : mapper.convert(type, SimpleDimensionType.class),
                options
            ));
        }
        authentication.addDimensions(dimensions);

        return this;

    }

    @Override
    public AuthenticationBuilder json(String json) {
        Map<String, Object> jsonObject = mapper.readAsMap(json, Object.class);
        user(mapper.convert(jsonObject.get("user"), SimpleUser.class));
        if (jsonObject.containsKey("roles")) {
            role((List) mapper.convert(jsonObject.get("roles"), ROLES));
        }
        if (jsonObject.containsKey("permissions")) {
            permission(new JSONArray((List) jsonObject.get("permissions")));
        }
        if (jsonObject.containsKey("dimensions")) {
            List values = (List) jsonObject.get("dimensions");
            dimension(values == null ? null : new JSONArray(values));
        }
        if (jsonObject.containsKey("attributes")) {
            attributes(Maps.transformValues((Map<String, Object>) jsonObject.get("attributes"), Serializable.class::cast));
        }
        return this;
    }

    @Override
    public Authentication build() {
        return authentication;
    }
}
