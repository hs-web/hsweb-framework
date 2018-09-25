package org.hswebframework.web.authorization.basic.embed;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.Role;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.authorization.simple.SimplePermission;
import org.hswebframework.web.authorization.simple.SimpleRole;
import org.hswebframework.web.authorization.simple.SimpleUser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * hsweb:
 *      users:
 *          admin:
 *            name: 超级管理员
 *            username: admin
 *            password: admin
 *            roles:
 *              - id: admin
 *                name: 管理员
 *              - id: user
 *                name: 用户
 *            permissions:
 *              - id: user-manager
 *                actions: *
 *                dataAccesses:
 *                  - action: query
 *                    type: DENY_FIELDS
 *                    fields: password,salt
 * </pre>
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Getter
@Setter
public class EmbedAuthenticationProperties {

    private String id;

    private String name;

    private String username;

    private String type;

    private String password;

    private List<SimpleRole> roles = new ArrayList<>();

    private List<PermissionInfo> permissions = new ArrayList<>();

    private Map<String, List<String>> permissionsSimple = new HashMap<>();

    @Getter
    @Setter
    public static class PermissionInfo {
        private String id;

        private Set<String> actions = new HashSet<>();

        private List<Map<String, Object>> dataAccesses = new ArrayList<>();
    }

    public Authentication toAuthentication(DataAccessConfigBuilderFactory factory) {
        SimpleAuthentication authentication = new SimpleAuthentication();
        SimpleUser user = new SimpleUser();
        user.setId(id);
        user.setName(name);
        user.setUsername(username);
        user.setType(type);
        authentication.setUser(user);
        authentication.setRoles((List) roles);
        List<Permission> permissionList = new ArrayList<>();

        permissionList.addAll(permissions.stream()
                .map(info -> {
                    SimplePermission permission = new SimplePermission();
                    permission.setId(info.getId());
                    permission.setActions(info.getActions());
                    permission.setDataAccesses(info.getDataAccesses()
                            .stream().map(conf -> factory.create()
                                    .fromJson(JSON.toJSONString(conf))
                                    .build()).collect(Collectors.toSet()));
                    return permission;

                })
                .collect(Collectors.toList()));

        permissionList.addAll(permissionsSimple.entrySet().stream()
                .map(entry -> {
                    SimplePermission permission = new SimplePermission();
                    permission.setId(entry.getKey());
                    permission.setActions(new HashSet<>(entry.getValue()));
                    return permission;
                }).collect(Collectors.toList()));

        authentication.setPermissions(permissionList);
        return authentication;
    }

}
