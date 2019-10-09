package org.hswebframework.web.authorization.basic.embed;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.AuthenticationRequest;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.validation.ValidationException;
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
                            Map<?, ?> mapVal = ((Map) stringObjectEntry.getValue());
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
    public Mono<Authentication> authenticate(Mono<AuthenticationRequest> request) {
        return request.filter(r -> r instanceof PlainTextUsernamePasswordAuthenticationRequest)
                .map(PlainTextUsernamePasswordAuthenticationRequest.class::cast)
                .map(pwdReq -> users.values()
                        .stream()
                        .filter(user ->
                                pwdReq.getUsername().equals(user.getUsername())
                                        && pwdReq.getPassword().equals(user.getPassword()))
                        .findFirst()
                        .map(EmbedAuthenticationProperties::getId)
                        .map(authentications::get)
                        .orElseThrow(() -> new ValidationException("用户不存在")));

    }

    @Override
    public Mono<Authentication> getByUserId(String userId) {
        return Mono.just(authentications.get(userId));
    }

    void addAuthentication(Authentication authentication) {
        authentications.put(authentication.getUser().getId(), authentication);
    }
}
