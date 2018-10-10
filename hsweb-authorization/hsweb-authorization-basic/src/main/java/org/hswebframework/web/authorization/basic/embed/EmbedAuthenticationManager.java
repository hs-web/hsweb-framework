package org.hswebframework.web.authorization.basic.embed;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.AuthenticationRequest;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.hswebframework.web.validate.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@ConfigurationProperties(prefix = "hsweb")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EmbedAuthenticationManager implements AuthenticationManager {

    private Map<String, Authentication> authentications = new HashMap<>();

    @Autowired(required = false)
    private DataAccessConfigBuilderFactory dataAccessConfigBuilderFactory = new SimpleDataAccessConfigBuilderFactory();

    @Getter
    @Setter
    private Map<String, EmbedAuthenticationProperties> users = new HashMap<>();

    @PostConstruct
    public void init() {
        users.forEach((id, properties) -> {
            if (StringUtils.isEmpty(properties.getId())) {
                properties.setId(id);
            }
            for (EmbedAuthenticationProperties.PermissionInfo permissionInfo : properties.getPermissions()) {
                for (Map<String, Object> objectMap : permissionInfo.getDataAccesses()) {
                    for (Map.Entry<String, Object> stringObjectEntry : objectMap.entrySet()) {
                        if (stringObjectEntry.getValue() instanceof Map) {
                            Map mapVal = ((Map) stringObjectEntry.getValue());
                            boolean maybeIsList = mapVal.keySet().stream().allMatch(org.hswebframework.utils.StringUtils::isInt);
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

    @Override
    public Authentication authenticate(AuthenticationRequest request) {
        if (request instanceof PlainTextUsernamePasswordAuthenticationRequest) {
            return sync(users.values().stream()
                    .filter(user ->
                            ((PlainTextUsernamePasswordAuthenticationRequest) request).getUsername().equals(user.getUsername())
                                    && ((PlainTextUsernamePasswordAuthenticationRequest) request).getPassword().equals(user.getPassword()))
                    .findFirst()
                    .map(properties -> authentications.get(properties.getId()))
                    .orElseThrow(() -> new ValidationException("用户不存在")));
        }

        throw new UnsupportedOperationException("不支持的授权类型:" + request);

    }

    @Override
    public Authentication getByUserId(String userId) {
        return authentications.get(userId);
    }

    @Override
    public Authentication sync(Authentication authentication) {
        authentications.put(authentication.getUser().getId(), authentication);
        return authentication;
    }

    void addAuthentication(Authentication authentication) {
        sync(authentication);
    }
}
