package org.hswebframework.web.system.authorization.defaults.service;

import com.google.common.cache.CacheBuilder;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.DimensionProvider;
import org.hswebframework.web.authorization.DefaultDimensionType;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.events.AuthorizationInitializeEvent;
import org.hswebframework.web.authorization.simple.SimpleDimension;
import org.hswebframework.web.authorization.simple.SimpleDimensionType;
import org.hswebframework.web.cache.supports.GuavaReactiveCacheManager;
import org.hswebframework.web.system.authorization.api.entity.AuthorizationSettingEntity;
import org.hswebframework.web.system.authorization.api.entity.DataAccessEntity;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** 初始化失败不得发布完成事件或把已加载的部分维度作为有效身份缓存。 */
public class DefaultReactiveAuthenticationInitializeServiceTest {

    @Test
    public void dimensionFailureDoesNotCachePartialIdentityAndCanRecover() {
        IllegalStateException failure = new IllegalStateException("dimension unavailable");
        AtomicBoolean failing = new AtomicBoolean(true);
        Dimension role = SimpleDimension.of("role", "role", DefaultDimensionType.role, Map.of());
        Dimension tenant = SimpleDimension.of("tenant", "tenant", SimpleDimensionType.of("tenant"), Map.of());
        DimensionProvider dimensions = mock(DimensionProvider.class);
        doReturn(Flux.defer(() -> Flux.concat(Flux.just(role),
            failing.get() ? Flux.error(failure) : Flux.just(tenant))))
            .when(dimensions).getDimensionByUserId("reader");
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        var initializer = initializer(events, Flux.empty(), Mono.just(Map.of()));
        ReflectionTestUtils.setField(initializer, "dimensionProviders", List.of(dimensions));
        ReactiveUserService users = mock(ReactiveUserService.class);
        AtomicInteger userReads = new AtomicInteger();
        when(users.findById("reader")).thenReturn(Mono.fromSupplier(() -> {
            userReads.incrementAndGet();
            return user();
        }));
        ReflectionTestUtils.setField(initializer, "userService", users);
        var cache = new GuavaReactiveCacheManager(CacheBuilder.newBuilder());
        var manager = new DefaultReactiveAuthenticationManager();
        ReflectionTestUtils.setField(manager, "initializeService", initializer);
        ReflectionTestUtils.setField(manager, "cacheManager", cache);

        StepVerifier.create(initializer.initUserAuthorization("reader"))
            .expectErrorMatches(error -> error == failure).verify();
        // 既有缓存适配器将 loader 异常转为空结果；必须不返回、不缓存部分身份。
        StepVerifier.create(manager.getByUserId("reader")).verifyComplete();
        StepVerifier.create(cache.getCache("user-auth").getMono("reader")).verifyComplete();
        verifyNoInteractions(events);

        failing.set(false);
        for (int i = 0; i < 2; i++) {
            StepVerifier.create(manager.getByUserId("reader"))
                .assertNext(auth -> {
                    assertTrue(auth.getDimension("tenant", "tenant").isPresent());
                    assertTrue(auth.getDimension(DefaultDimensionType.role, "role").isPresent());
                }).verifyComplete();
        }
        assertEquals(3, userReads.get());
        verify(events).publishEvent(any(AuthorizationInitializeEvent.class));
    }

    @Test
    public void settingsReadFailureDoesNotPublishPartialIdentity() {
        IllegalStateException failure = new IllegalStateException("settings unavailable");
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        StepVerifier.create(initializer(events, Flux.error(failure), Mono.just(Map.of())).doInit(Mono.just(user())))
            .expectErrorMatches(error -> error == failure).verify();
        verifyNoInteractions(events);
    }

    @Test
    public void permissionDefinitionReadFailureDoesNotPublishPartialIdentity() {
        IllegalStateException failure = new IllegalStateException("permissions unavailable");
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        StepVerifier.create(initializer(events, Flux.just(setting()), Mono.error(failure)).doInit(Mono.just(user())))
            .expectErrorMatches(error -> error == failure).verify();
        verifyNoInteractions(events);
    }

    @Test
    public void permissionAssemblyFailureDoesNotPublishPartialIdentity() {
        IllegalStateException failure = new IllegalStateException("invalid data permission");
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        var setting = setting();
        setting.setDataAccesses(List.of(new DataAccessEntity()));
        var definition = new PermissionEntity();
        definition.setId("device");
        var initializer = initializer(events, Flux.just(setting), Mono.just(Map.of("device", definition)));
        var builders = mock(DataAccessConfigBuilderFactory.class);
        when(builders.create()).thenThrow(failure);
        ReflectionTestUtils.setField(initializer, "builderFactory", builders);
        StepVerifier.create(initializer.doInit(Mono.just(user())))
            .expectErrorMatches(error -> error == failure).verify();
        verifyNoInteractions(events);
    }

    @Test
    public void genuinelyEmptyDimensionsRemainUsable() {
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        StepVerifier.create(initializer(events, Flux.empty(), Mono.just(Map.of())).doInit(Mono.just(user())))
            .assertNext(auth -> {
                assertEquals("reader", auth.getUser().getId());
                assertTrue(auth.getDimensions("tenant").isEmpty());
                assertTrue(auth.getPermissions().isEmpty());
            }).verifyComplete();
        verify(events).publishEvent(any(AuthorizationInitializeEvent.class));
    }

    @Test
    public void disabledAndMissingUsersDoNotPublishCompletion() {
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        var initializer = initializer(events, Flux.empty(), Mono.just(Map.of()));
        UserEntity disabled = user();
        disabled.setStatus(UserEntity.STATUS_DISABLED);
        StepVerifier.create(initializer.doInit(Mono.just(disabled))).verifyComplete();
        StepVerifier.create(initializer.doInit(Mono.empty())).verifyComplete();
        verifyNoInteractions(events);
    }

    @Test
    public void initializationListenerFailurePropagates() {
        IllegalStateException failure = new IllegalStateException("customizer unavailable");
        ApplicationEventPublisher events = event -> ((AuthorizationInitializeEvent) event).async(Mono.error(failure));
        StepVerifier.create(initializer(events, Flux.empty(), Mono.just(Map.of())).doInit(Mono.just(user())))
            .expectErrorMatches(error -> error == failure).verify();
    }

    private static DefaultReactiveAuthenticationInitializeService initializer(ApplicationEventPublisher events,
        Flux<AuthorizationSettingEntity> settings, Mono<Map<String, PermissionEntity>> definitions) {
        var initializer = new DefaultReactiveAuthenticationInitializeService() {
            @Override
            protected Flux<AuthorizationSettingEntity> getSettings(List<Dimension> dimensions) {
                return settings;
            }

            @Override
            protected Mono<Map<String, PermissionEntity>> getAllPermission() {
                return definitions;
            }
        };
        ReflectionTestUtils.setField(initializer, "eventPublisher", events);
        return initializer;
    }

    private static UserEntity user() {
        UserEntity user = new UserEntity();
        user.setId("reader"); user.setUsername("reader"); user.setStatus(UserEntity.STATUS_ENABLED);
        return user;
    }

    private static AuthorizationSettingEntity setting() {
        var setting = new AuthorizationSettingEntity();
        setting.setPermission("device");
        return setting;
    }
}
