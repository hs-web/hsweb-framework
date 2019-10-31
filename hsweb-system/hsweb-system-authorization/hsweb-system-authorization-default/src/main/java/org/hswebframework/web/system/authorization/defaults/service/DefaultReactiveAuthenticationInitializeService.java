package org.hswebframework.web.system.authorization.defaults.service;

import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.ReactiveAuthenticationInitializeService;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessType;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.authorization.simple.SimplePermission;
import org.hswebframework.web.authorization.simple.SimpleUser;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.DimensionProvider;
import org.hswebframework.web.system.authorization.api.entity.AuthorizationSettingEntity;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultReactiveAuthenticationInitializeService
        implements ReactiveAuthenticationInitializeService {

    @Autowired
    private ReactiveUserService userService;

    @Autowired
    private ReactiveRepository<AuthorizationSettingEntity, String> settingRepository;

    @Autowired
    private ReactiveRepository<PermissionEntity, String> permissionRepository;

    @Autowired(required = false)
    private DataAccessConfigBuilderFactory builderFactory = new SimpleDataAccessConfigBuilderFactory();

    @Autowired(required = false)
    private List<DimensionProvider> dimensionProviders = new ArrayList<>();

    @Override
    public Mono<Authentication> initUserAuthorization(String userId) {
        return doInit(userService.findById(userId));
    }

    public Mono<Authentication> doInit(Mono<UserEntity> userEntityMono) {

        return userEntityMono.flatMap(user -> {
            SimpleAuthentication authentication = new SimpleAuthentication();
            authentication.setUser(SimpleUser
                    .builder()
                    .id(user.getId())
                    .name(user.getName())
                    .username(user.getUsername())
                    .userType(user.getType())
                    .build());
            return initPermission(authentication);
        });

    }

    protected Flux<AuthorizationSettingEntity> getSettings(List<Dimension> dimensions) {
        return Flux.fromIterable(dimensions)
                .groupBy(d -> d.getType() == null ? "unknown" : d.getType().getId(), (Function<Dimension, Object>) Dimension::getId)
                .flatMap(group ->
                        group.collectList()
                                .flatMapMany(list -> settingRepository
                                        .createQuery()
                                        .where(AuthorizationSettingEntity::getState, 1)
                                        .and(AuthorizationSettingEntity::getDimensionType, group.key())
                                        .in(AuthorizationSettingEntity::getDimensionTarget, list)
                                        .fetch()));
    }

    protected Mono<Authentication> initPermission(SimpleAuthentication authentication) {
        return Flux.fromIterable(dimensionProviders)
                .flatMap(provider -> provider.getDimensionByUserId(authentication.getUser().getId()))
                .collectList()
                .doOnNext(authentication::setDimensions)
                .flatMap(allDimension ->
                        Mono.zip(
                                getAllPermission()
                                , getSettings(allDimension).collect(Collectors.groupingBy(AuthorizationSettingEntity::getPermission))
                                , (_p, _s) -> handlePermission(authentication, allDimension, _p, _s)
                        ));

    }

    protected SimpleAuthentication handlePermission(SimpleAuthentication authentication,
                                                    List<Dimension> dimensionList,
                                                    Map<String, PermissionEntity> permissions,
                                                    Map<String, List<AuthorizationSettingEntity>> settings) {
        List<Permission> permissionList = new ArrayList<>();

        for (PermissionEntity value : permissions.values()) {
            List<AuthorizationSettingEntity> permissionSettings = settings.get(value.getId());
            if (CollectionUtils.isEmpty(permissionSettings)) {
                continue;
            }
            permissionSettings.sort(Comparator.comparingInt(e -> e.getPriority() == null ? 0 : e.getPriority()));
            SimplePermission permission = new SimplePermission();
            permission.setId(value.getId());
            permission.setName(value.getName());
            Map<DataAccessType, DataAccessConfig> configs = new HashMap<>();

            for (AuthorizationSettingEntity permissionSetting : permissionSettings) {

                boolean merge = Boolean.TRUE.equals(permissionSetting.getMerge());

                if (!merge) {
                    permission.getActions().clear();
                }

                if (permissionSetting.getDataAccesses() != null) {
                    permissionSetting.getDataAccesses()
                            .stream()
                            .map(conf -> builderFactory.create().fromMap(conf.getConfig()).build())
                            .forEach(access -> configs.put(access.getType(), access));
                }

                permission.getActions().addAll(permissionSetting.getActions());

            }
            permission.setDataAccesses(new HashSet<>(configs.values()));
            permissionList.add(permission);

        }
        authentication.setPermissions(permissionList);

        return authentication;
    }

    protected Mono<Map<String, PermissionEntity>> getAllPermission() {

        return permissionRepository
                .createQuery()
                .where(PermissionEntity::getStatus, 1)
                .fetch()
                .collect(Collectors.toMap(PermissionEntity::getId, Function.identity()));
    }

}
