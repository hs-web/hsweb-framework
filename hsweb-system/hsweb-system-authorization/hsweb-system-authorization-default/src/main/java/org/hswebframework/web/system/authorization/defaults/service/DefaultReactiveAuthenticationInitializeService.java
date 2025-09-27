package org.hswebframework.web.system.authorization.defaults.service;

import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.ReactiveAuthenticationInitializeService;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.events.AuthorizationInitializeEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
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

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private AuthenticationInitializeProperties properties;


    private Mono<Map<String, PermissionEntity>> allPermissionCache;

    private final Map<Tuple2<String, Collection<String>>, Flux<AuthorizationSettingEntity>> settingCache =
        CacheBuilder.newBuilder()
                    .expireAfterAccess(Duration.ofSeconds(10))
                    .<Tuple2<String, Collection<String>>, Flux<AuthorizationSettingEntity>>build()
                    .asMap();

    @Override
    @Transactional
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

            return initPermission(authentication)
                .defaultIfEmpty(authentication)
                .onErrorResume(err -> {
                    log.warn(err.getMessage(), err);
                    return Mono.just(authentication);
                })
                .flatMap(auth -> {
                    AuthorizationInitializeEvent event = new AuthorizationInitializeEvent(auth);
                    return event
                        .publish(eventPublisher)
                        .then(Mono.fromSupplier(event::getAuthentication));
                });
        });
    }

    protected Flux<AuthorizationSettingEntity> getSettings(List<Dimension> dimensions) {
        return Flux
            .fromIterable(dimensions)
            .filter(dimension -> dimension.getType() != null
                && properties.isDimensionEnabled(dimension.getType().getId()))
            .groupBy(d -> d.getType().getId(), Dimension::getId)
            .flatMap(group ->
                         group.buffer(200)
                              .sort()
                              .concatMap(list -> findSettings(group.key(), list)));
    }

    protected Flux<AuthorizationSettingEntity> findSettings(String type, List<String> target) {
        return settingCache
            .computeIfAbsent(
                Tuples.of(type, target),
                tp2 -> settingRepository
                    .createQuery()
                    .where(AuthorizationSettingEntity::getState, 1)
                    .and(AuthorizationSettingEntity::getDimensionType, tp2.getT1())
                    .in(AuthorizationSettingEntity::getDimensionTarget, tp2.getT2())
                    .fetch()
                    .cache(Duration.ofSeconds(1))
            );
    }

    protected Mono<Authentication> initPermission(SimpleAuthentication authentication) {
        return Flux.fromIterable(dimensionProviders)
                   .flatMap(provider -> provider.getDimensionByUserId(authentication.getUser().getId()))
                   .cast(Dimension.class)
                   //去重?还是合并?
                   .distinct(dis -> Tuples.of(dis.getType().getId(), dis.getId()))
                   .doOnNext(authentication::addDimension)
//                   .collectList()
                   .then(Mono.defer(() -> this
                       .getSettings(authentication.getDimensions())
                       .collect(Collectors.groupingBy(AuthorizationSettingEntity::getPermission))
                       .flatMap(_s -> {
                           // 没有任何setting,则直接返回
                           if (_s.isEmpty()) {
                               return Mono.just(authentication);
                           } else {
                               return getAllPermission()
                                   .map(_p -> handlePermission(authentication, _p, _s));
                           }
                       })));

    }

    protected SimpleAuthentication handlePermission(SimpleAuthentication authentication,
                                                    Map<String, PermissionEntity> permissions,
                                                    Map<String, List<AuthorizationSettingEntity>> settings) {
        if (settings.isEmpty()) {
            return authentication;
        }
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
                permission.setOptions(permissionEntity.getProperties());
                Set<DataAccessConfig> configs = new HashSet<>();

                for (AuthorizationSettingEntity permissionSetting : permissionSettings) {

                    boolean merge = Boolean.TRUE.equals(permissionSetting.getMerge());

                    if (!merge) {
                        permission.getActions().clear();
                    }

                    if (permissionSetting.getDataAccesses() != null) {
                        permissionSetting.getDataAccesses()
                                         .stream()
                                         .map(conf -> {
                                             DataAccessConfig config = builderFactory
                                                 .create()
                                                 .fromMap(conf.toMap())
                                                 .build();
                                             if (config == null) {
                                                 log.warn("unsupported data access:{}", conf.toMap());
                                             }
                                             return config;
                                         })
                                         .filter(Objects::nonNull)
                                         .forEach(configs::add);
                    }
                    if (CollectionUtils.isNotEmpty(permissionSetting.getActions())) {
                        permission.getActions().addAll(permissionSetting.getActions());
                    }

                }
                allowed.put(permissionEntity.getId(), permission);
                permission.setDataAccesses(configs);
            }

            //处理关联权限
            for (PermissionEntity permissionEntity : permissions.values()) {
                SimplePermission allow = allowed.get(permissionEntity.getId());
                if (allow == null || CollectionUtils.isEmpty(permissionEntity.getParents())) {
                    continue;
                }
                for (ParentPermission parent : permissionEntity.getParents()) {
                    if (ObjectUtils.isEmpty(parent.getPermission())) {
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
            log.error(e.getLocalizedMessage(), e);
        }
        return authentication;
    }


    protected Mono<Map<String, PermissionEntity>> getAllPermission() {
        Mono<Map<String, PermissionEntity>> allPermissionCache = this.allPermissionCache;
        if (allPermissionCache == null) {
            return this.allPermissionCache = Mono
                .defer(() -> permissionRepository
                    .createQuery()
                    .where(PermissionEntity::getStatus, 1)
                    .fetch()
                    .collect(Collectors.toMap(PermissionEntity::getId, Function.identity()))
                    .switchIfEmpty(Mono.just(Collections.emptyMap())))
                .cache(Duration.ofSeconds(1));
        }
        return allPermissionCache;
    }

}
