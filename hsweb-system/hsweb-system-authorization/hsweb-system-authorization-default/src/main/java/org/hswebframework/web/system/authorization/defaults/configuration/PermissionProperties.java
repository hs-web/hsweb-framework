package org.hswebframework.web.system.authorization.defaults.configuration;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.system.authorization.api.entity.AuthorizationSettingEntity;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = "hsweb.permission")
public class PermissionProperties {

    private PermissionFilter filter = new PermissionFilter();

    @Getter
    @Setter
    public static class PermissionFilter {
        //开启权限过滤
        private boolean enabled = false;
        //越权赋权时处理逻辑
        private UnAuthStrategy unAuthStrategy = UnAuthStrategy.error;

        private Set<String> excludeUsername = new HashSet<>();

        public AuthorizationSettingEntity handleSetting(Authentication authentication,
                                                        AuthorizationSettingEntity setting) {
            if (!enabled || excludeUsername.contains(authentication.getUser().getUsername())) {
                return setting;
            }
            //有全部权限
            if (authentication.hasPermission(setting.getPermission(), setting.getActions())) {
                return setting;
            }
            //交给具体的策略处理
            return unAuthStrategy.handle(authentication, setting);
        }

        public Flux<PermissionEntity> doFilter(Flux<PermissionEntity> flux, Authentication authentication) {
            if (!enabled || excludeUsername.contains(authentication.getUser().getUsername())) {
                return flux;
            }
            return flux
                    .map(entity -> entity
                            .copy(action -> authentication.hasPermission(entity.getId(), action.getAction()),
                                  optionalField -> true))
                    .filter(entity -> !CollectionUtils.isEmpty(entity.getActions()));
        }

        public enum UnAuthStrategy {
            //忽略赋权
            ignore {
                @Override
                public AuthorizationSettingEntity handle(Authentication authentication, AuthorizationSettingEntity setting) {

                    return setting.copy(action -> authentication.hasPermission(setting.getPermission(), action), access -> true);
                }
            },
            //抛出错误
            error {
                @Override
                public AuthorizationSettingEntity handle(Authentication authentication, AuthorizationSettingEntity setting) {
                    Set<String> actions = new HashSet<>(setting.getActions());
                    actions.removeAll(authentication
                                              .getPermission(setting.getPermission())
                                              .map(Permission::getActions)
                                              .orElseGet(Collections::emptySet));

                    throw new AccessDenyException("当前用户无权限:" + setting.getPermission() + "" +actions);
                }
            };

            public abstract AuthorizationSettingEntity handle(Authentication authentication,
                                                              AuthorizationSettingEntity setting);
        }
    }
}
