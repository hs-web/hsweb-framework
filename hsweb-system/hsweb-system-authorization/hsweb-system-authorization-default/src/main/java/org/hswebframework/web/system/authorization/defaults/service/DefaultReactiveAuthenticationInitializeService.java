package org.hswebframework.web.system.authorization.defaults.service;

import lombok.extern.slf4j.Slf4j;
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
import org.hswebframework.web.system.authorization.api.entity.ParentPermission;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
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
                .filter(dimension -> dimension.getType() != null)
                .groupBy(d -> d.getType().getId(), (Function<Dimension, Object>) Dimension::getId)
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
                .cast(Dimension.class)
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
        Map<String, PermissionEntity> permissionMap = new HashMap<>();
        Map<String, SimplePermission> allowed = new HashMap<>();
        try {
            for (PermissionEntity permissionEntity : permissions.values()) {
                permissionMap.put(permissionEntity.getId(), permissionEntity);
                List<AuthorizationSettingEntity> permissionSettings = settings.get(permissionEntity.getId());
                if (CollectionUtils.isEmpty(permissionSettings)) {
                    continue;
                }
                permissionSettings.sort(Comparator.comparingInt(e -> e.getPriority() == null ? 0 : e.getPriority()));
                SimplePermission permission = new SimplePermission();
                permission.setId(permissionEntity.getId());
                permission.setName(permissionEntity.getName());
                Map<DataAccessType, DataAccessConfig> configs = new HashMap<>();

                for (AuthorizationSettingEntity permissionSetting : permissionSettings) {

                    boolean merge = Boolean.TRUE.equals(permissionSetting.getMerge());

                    if (!merge) {
                        permission.getActions().clear();
                    }

                    if (permissionSetting.getDataAccesses() != null) {
                        permissionSetting.getDataAccesses()
                                .stream()
                                .map(conf -> {
                                    DataAccessConfig config = builderFactory.create().fromMap(conf.toMap()).build();
                                    if (config == null) {
                                        log.warn("unsupported data access:{}", conf.toMap());
                                    }
                                    return config;
                                })
                                .filter(Objects::nonNull)
                                .forEach(access -> configs.put(access.getType(), access));
                    }
                    if (CollectionUtils.isNotEmpty(permissionSetting.getActions())) {
                        permission.getActions().addAll(permissionSetting.getActions());
                    }

                }
                allowed.put(permissionEntity.getId(), permission);
                permission.setDataAccesses(new HashSet<>(configs.values()));
            }

            //处理关联权限
            for (PermissionEntity permissionEntity : permissions.values()) {
                SimplePermission allow = allowed.get(permissionEntity.getId());
                if (allow == null || CollectionUtils.isEmpty(permissionEntity.getParents())) {
                    continue;
                }
                for (ParentPermission parent : permissionEntity.getParents()) {
                    if (StringUtils.isEmpty(parent.getPermission())) {
                        continue;
                    }
                    Set<String> pre = parent.getPreActions();
                    //满足前置条件
                    if (CollectionUtils.isEmpty(pre) || allow.getActions().containsAll(pre)) {
                        PermissionEntity mergePermission = permissionMap.get(parent.getPermission());
                        if (mergePermission == null) {
                            continue;
                        }
                        SimplePermission merge = allowed.get(parent.getPermission());
                        if (merge == null) {
                            merge = new SimplePermission();
                            merge.setName(mergePermission.getName());
                            merge.setId(mergePermission.getId());
                            allowed.put(merge.getId(), merge);
                        }
                        if (CollectionUtils.isNotEmpty(parent.getActions())) {
                            merge.getActions().addAll(parent.getActions());
                        }
                    }
                }
            }
            authentication.setPermissions(new ArrayList<>(allowed.values()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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
