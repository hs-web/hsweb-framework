package org.hswebframework.web.authorization.basic.embed;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationRequest;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <pre>
 * hsweb:
 *    auth:
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
@ConfigurationProperties(prefix = "hsweb.auth")
public class EmbedAuthenticationProperties implements InitializingBean {

    private Map<String, Authentication> authentications = new HashMap<>();

    @Getter
    @Setter
    private Map<String, EmbedAuthenticationInfo> users = new HashMap<>();

    @Autowired(required = false)
    private DataAccessConfigBuilderFactory dataAccessConfigBuilderFactory = new SimpleDataAccessConfigBuilderFactory();

    @Override
    public void afterPropertiesSet() {
        users.forEach((id, properties) -> {
            if (StringUtils.isEmpty(properties.getId())) {
                properties.setId(id);
            }
            for (EmbedAuthenticationInfo.PermissionInfo permissionInfo : properties.getPermissions()) {
                for (Map<String, Object> objectMap : permissionInfo.getDataAccesses()) {
                    for (Map.Entry<String, Object> stringObjectEntry : objectMap.entrySet()) {
                        if (stringObjectEntry.getValue() instanceof Map) {
                            Map<?, ?> mapVal = ((Map) stringObjectEntry.getValue());
                            boolean maybeIsList = mapVal
                                    .keySet()
                                    .stream()
                                    .allMatch(org.hswebframework.utils.StringUtils::isInt);
                            if (maybeIsList) {
                                stringObjectEntry.setValue(mapVal.values());
                            }
                        }
                    }
                }
            }
            authentications.put(id, properties.toAuthentication(dataAccessConfigBuilderFactory));
        });
    }

    public Authentication authenticate(AuthenticationRequest request) {
        if (MapUtils.isEmpty(users)) {
            return null;
        }
        if (request instanceof PlainTextUsernamePasswordAuthenticationRequest) {
            PlainTextUsernamePasswordAuthenticationRequest pwdReq = ((PlainTextUsernamePasswordAuthenticationRequest) request);
            for (EmbedAuthenticationInfo user : users.values()) {
                if (pwdReq.getUsername().equals(user.getUsername())) {
                    if (pwdReq.getPassword().equals(user.getPassword())) {
                        return user.toAuthentication(dataAccessConfigBuilderFactory);
                    }
                    return null;
                }
            }
            return null;
        }

        throw new UnsupportedOperationException("不支持的授权请求:" + request);
    }

    public Optional<Authentication> getAuthentication(String userId) {
        return Optional.ofNullable(authentications.get(userId));
    }


}
