package org.hswebframework.web.system.authorization.defaults.service.reactive;

import com.google.common.cache.CacheBuilder;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.ReactiveAuthenticationInitializeService;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.events.AuthorizationInitializeEvent;
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.cache.ReactiveCacheManager;
import org.hswebframework.web.cache.supports.GuavaReactiveCacheManager;
import org.hswebframework.web.system.authorization.api.entity.ActionEntity;
import org.hswebframework.web.system.authorization.api.entity.AuthorizationSettingEntity;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.event.ClearUserAuthorizationCacheEvent;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.hswebframework.web.system.authorization.defaults.service.AuthenticationInitializeProperties;
import org.hswebframework.web.system.authorization.defaults.service.DefaultReactiveAuthenticationInitializeService;
import org.hswebframework.web.system.authorization.defaults.service.DefaultReactiveAuthenticationManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveTestApplication.class)
public class DefaultReactiveAuthenticationManagerTest {

    @Autowired
    private ReactiveUserService userService;

    @Autowired
    private ReactiveAuthenticationManager reactiveAuthenticationManager;

    @Autowired
    private ReactiveRepository<PermissionEntity, String> permissionRepository;

    @Autowired
    private ReactiveRepository<AuthorizationSettingEntity, String> settingRepository;

    @Test
    public void initializesPermissionsForNonDisabledUsersWithoutCache() {
        String userId = "state-check";
        String permissionId = "non-disabled-state-permission";
        grantUserPermission(userId, permissionId);
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setStatus(UserEntity.STATUS_DISABLED);
        AtomicInteger userReads = new AtomicInteger();
        AtomicInteger permissions = new AtomicInteger();
        AtomicInteger events = new AtomicInteger();
        ReactiveUserService users = mock(ReactiveUserService.class);
        when(users.findById(userId)).thenReturn(Mono.defer(() -> {
            userReads.incrementAndGet();
            return Mono.just(user);
        }));
        when(users.findById("missing")).thenReturn(Mono.defer(() -> {
            userReads.incrementAndGet();
            return Mono.empty();
        }));
        DefaultReactiveAuthenticationInitializeService initializer = createInitializer(users, permissions, events);
        DefaultReactiveAuthenticationManager manager = createManager(users, initializer, null);

        manager.getByUserId("missing")
               .as(StepVerifier::create)
               .verifyComplete();
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .verifyComplete();
        Assert.assertEquals(2, userReads.get());
        Assert.assertEquals(0, permissions.get());
        Assert.assertEquals(0, events.get());

        Byte[] readableStates = {UserEntity.STATUS_ENABLED, UserEntity.STATUS_LOCKED, null, (byte) 3};
        for (Byte status : readableStates) {
            user.setStatus(status);
            manager.getByUserId(userId)
                   .as(StepVerifier::create)
                   .assertNext(authentication -> {
                       assertUserStatus(authentication, userId, status);
                       Assert.assertTrue(authentication.hasPermission(permissionId, "query"));
                   })
                   .verifyComplete();
        }
        Assert.assertEquals(6, userReads.get());
        Assert.assertEquals(4, permissions.get());
        Assert.assertEquals(4, events.get());
    }

    @Test
    public void reloadsEnabledUsersAfterCacheEvictionWithoutReadingWarmUsers() {
        String userId = "cached-state-check";
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setStatus(UserEntity.STATUS_ENABLED);
        AtomicInteger userReads = new AtomicInteger();
        AtomicInteger permissions = new AtomicInteger();
        AtomicInteger events = new AtomicInteger();
        ReactiveUserService users = mock(ReactiveUserService.class);
        when(users.findById(userId)).thenReturn(Mono.defer(() -> {
            userReads.incrementAndGet();
            return Mono.just(user);
        }));
        when(users.findById("missing")).thenReturn(Mono.defer(() -> {
            userReads.incrementAndGet();
            return Mono.empty();
        }));
        DefaultReactiveAuthenticationInitializeService initializer = createInitializer(users, permissions, events);
        DefaultReactiveAuthenticationManager manager = createManager(
            users, initializer, new GuavaReactiveCacheManager(CacheBuilder.newBuilder()));

        for (int i = 0; i < 3; i++) {
            manager.getByUserId(userId)
                   .as(StepVerifier::create)
                   .assertNext(authentication -> assertUserStatus(authentication, userId, UserEntity.STATUS_ENABLED))
                   .verifyComplete();
        }
        Assert.assertEquals(1, userReads.get());
        Assert.assertEquals(1, permissions.get());
        Assert.assertEquals(1, events.get());

        user.setStatus(UserEntity.STATUS_DISABLED);
        clearCache(manager, userId);
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .verifyComplete();
        Assert.assertEquals(2, userReads.get());
        Assert.assertEquals(1, permissions.get());
        Assert.assertEquals(1, events.get());

        user.setStatus(UserEntity.STATUS_LOCKED);
        clearCache(manager, userId);
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .assertNext(authentication -> assertUserStatus(authentication, userId, UserEntity.STATUS_LOCKED))
               .verifyComplete();
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .assertNext(authentication -> assertUserStatus(authentication, userId, UserEntity.STATUS_LOCKED))
               .verifyComplete();
        Assert.assertEquals(3, userReads.get());
        Assert.assertEquals(2, permissions.get());
        Assert.assertEquals(2, events.get());

        user.setStatus(UserEntity.STATUS_ENABLED);
        clearCache(manager, userId);
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .assertNext(authentication -> assertUserStatus(authentication, userId, UserEntity.STATUS_ENABLED))
               .verifyComplete();
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .assertNext(authentication -> assertUserStatus(authentication, userId, UserEntity.STATUS_ENABLED))
               .verifyComplete();
        Assert.assertEquals(4, userReads.get());
        Assert.assertEquals(3, permissions.get());
        Assert.assertEquals(3, events.get());

        user.setStatus(null);
        clearCache(manager, userId);
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .assertNext(authentication -> assertUserStatus(authentication, userId, null))
               .verifyComplete();
        Assert.assertEquals(5, userReads.get());
        Assert.assertEquals(4, permissions.get());
        Assert.assertEquals(4, events.get());

        manager.getByUserId("missing")
               .as(StepVerifier::create)
               .verifyComplete();
        Assert.assertEquals(6, userReads.get());
        Assert.assertEquals(4, permissions.get());
        Assert.assertEquals(4, events.get());
    }

    @Test
    public void passwordAuthenticationOnlyAcceptsEnabledUsers() {
        String userId = "password-state-check";
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername(userId);
        AtomicInteger userReads = new AtomicInteger();
        AtomicInteger permissions = new AtomicInteger();
        AtomicInteger events = new AtomicInteger();
        ReactiveUserService users = mock(ReactiveUserService.class);
        when(users.findById(userId)).thenReturn(Mono.defer(() -> {
            userReads.incrementAndGet();
            return Mono.just(user);
        }));
        when(users.findByUsernameAndPassword(userId, "password"))
            .thenReturn(Mono.defer(() -> Mono.just(user)));
        DefaultReactiveAuthenticationManager manager = createManager(
            users, createInitializer(users, permissions, events),
            new GuavaReactiveCacheManager(CacheBuilder.newBuilder()));

        Byte[] rejectedStates = {UserEntity.STATUS_DISABLED, UserEntity.STATUS_LOCKED, null, (byte) 3};
        for (Byte status : rejectedStates) {
            user.setStatus(status);
            manager.authenticate(Mono.just(new PlainTextUsernamePasswordAuthenticationRequest(userId, "password")))
                   .as(StepVerifier::create)
                   .verifyComplete();
        }
        Assert.assertEquals(0, userReads.get());
        Assert.assertEquals(0, permissions.get());
        Assert.assertEquals(0, events.get());

        user.setStatus(UserEntity.STATUS_ENABLED);
        manager.authenticate(Mono.just(new PlainTextUsernamePasswordAuthenticationRequest(userId, "password")))
               .as(StepVerifier::create)
               .assertNext(authentication -> assertUserStatus(authentication, userId, UserEntity.STATUS_ENABLED))
               .verifyComplete();

        user.setStatus(UserEntity.STATUS_LOCKED);
        clearCache(manager, userId);
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .assertNext(authentication -> assertUserStatus(authentication, userId, UserEntity.STATUS_LOCKED))
               .verifyComplete();
        manager.authenticate(Mono.just(new PlainTextUsernamePasswordAuthenticationRequest(userId, "password")))
               .as(StepVerifier::create)
               .verifyComplete();

        user.setStatus(UserEntity.STATUS_ENABLED);
        clearCache(manager, userId);
        for (int i = 0; i < 2; i++) {
            manager.authenticate(Mono.just(new PlainTextUsernamePasswordAuthenticationRequest(userId, "password")))
                   .as(StepVerifier::create)
                   .assertNext(authentication -> assertUserStatus(authentication, userId, UserEntity.STATUS_ENABLED))
                   .verifyComplete();
        }
        Assert.assertEquals(3, userReads.get());
        Assert.assertEquals(3, permissions.get());
        Assert.assertEquals(3, events.get());
    }

    @Test
    public void propagatesUserLookupErrorsWithoutCache() {
        String userId = "lookup-error";
        IllegalStateException failure = new IllegalStateException("database unavailable");
        ReactiveUserService users = mock(ReactiveUserService.class);
        AtomicInteger permissions = new AtomicInteger();
        AtomicInteger events = new AtomicInteger();
        DefaultReactiveAuthenticationInitializeService initializer = createInitializer(users, permissions, events);
        when(users.findById(userId)).thenReturn(Mono.error(failure));
        DefaultReactiveAuthenticationManager manager = createManager(users, initializer, null);

        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .expectErrorSatisfies(error -> Assert.assertSame(failure, error))
               .verify();
        Assert.assertEquals(0, permissions.get());
        Assert.assertEquals(0, events.get());
    }

    @Test
    public void delegatesToCustomInitializerWithoutPersistentUserLookup() {
        String userId = "external-user";
        ReactiveUserService users = mock(ReactiveUserService.class);
        ReactiveAuthenticationInitializeService initializer = mock(ReactiveAuthenticationInitializeService.class);
        Authentication authentication = mock(Authentication.class);
        when(initializer.initUserAuthorization(userId)).thenReturn(Mono.just(authentication));
        DefaultReactiveAuthenticationManager manager = createManager(users, initializer, null);

        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .expectNext(authentication)
               .verifyComplete();
        verify(initializer).initUserAuthorization(userId);
        verifyNoInteractions(users);
    }

    private static void assertUserStatus(Authentication authentication, String userId, Byte status) {
        Assert.assertEquals(userId, authentication.getUser().getId());
        Assert.assertTrue(authentication.getUser().getOptions().containsKey(UserEntity.OPTION_STATUS));
        Assert.assertEquals(status, authentication.getUser().getOptions().get(UserEntity.OPTION_STATUS));
    }

    private void grantUserPermission(String userId, String permissionId) {
        permissionRepository.newInstance()
                            .map(permission -> {
                                permission.setId(permissionId);
                                permission.setName(permissionId);
                                permission.setActions(Collections.singletonList(
                                    ActionEntity.builder().action("query").describe("query").build()));
                                permission.setStatus((byte) 1);
                                return permission;
                            })
                            .as(permissionRepository::insert)
                            .as(StepVerifier::create)
                            .expectNext(1)
                            .verifyComplete();
        settingRepository.newInstance()
                         .map(setting -> {
                             setting.setPermission(permissionId);
                             setting.setActions(Collections.singleton("query"));
                             setting.setDimensionType("user");
                             setting.setDimensionTarget(userId);
                             setting.setState((byte) 1);
                             return setting;
                         })
                         .as(settingRepository::insert)
                         .as(StepVerifier::create)
                         .expectNext(1)
                         .verifyComplete();
    }

    private DefaultReactiveAuthenticationInitializeService createInitializer(
        ReactiveUserService users, AtomicInteger permissions, AtomicInteger events) {
        DefaultReactiveAuthenticationInitializeService initializer = new DefaultReactiveAuthenticationInitializeService() {
            @Override
            protected Mono<Authentication> initPermission(SimpleAuthentication authentication) {
                permissions.incrementAndGet();
                return super.initPermission(authentication);
            }
        };
        ApplicationEventPublisher publisher = event -> {
            Assert.assertTrue(event instanceof AuthorizationInitializeEvent);
            events.incrementAndGet();
        };
        ReflectionTestUtils.setField(initializer, "userService", users);
        ReflectionTestUtils.setField(initializer, "settingRepository", settingRepository);
        ReflectionTestUtils.setField(initializer, "permissionRepository", permissionRepository);
        ReflectionTestUtils.setField(initializer, "properties", new AuthenticationInitializeProperties());
        ReflectionTestUtils.setField(initializer, "eventPublisher", publisher);
        return initializer;
    }

    private static DefaultReactiveAuthenticationManager createManager(
        ReactiveUserService users,
        ReactiveAuthenticationInitializeService initializer,
        ReactiveCacheManager caches) {
        DefaultReactiveAuthenticationManager manager = new DefaultReactiveAuthenticationManager();
        ReflectionTestUtils.setField(manager, "reactiveUserService", users);
        ReflectionTestUtils.setField(manager, "initializeService", initializer);
        ReflectionTestUtils.setField(manager, "cacheManager", caches);
        return manager;
    }

    private static void clearCache(DefaultReactiveAuthenticationManager manager, String userId) {
        ClearUserAuthorizationCacheEvent event = ClearUserAuthorizationCacheEvent.of(userId).useAsync();
        manager.handleClearAuthCache(event);
        event.getAsync()
             .as(StepVerifier::create)
             .verifyComplete();
    }

    @Test
    public void test() {
        UserEntity entity = new UserEntity();
        entity.setName("admin");
        entity.setUsername("admin");
        entity.setPassword("admin");

        userService.saveUser(Mono.just(entity))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        permissionRepository.newInstance()
                .map(permission -> {
                    permission.setId("test");
                    permission.setName("测试");
                    permission.setActions(Arrays.asList(ActionEntity.builder().action("add").describe("新增").build()));
                    permission.setStatus((byte) 1);
                    return permission;
                })
                .as(permissionRepository::insert)
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

        settingRepository.newInstance()
                .map(setting -> {
                    setting.setPermission("test");
                    setting.setActions(Collections.singleton("add"));
                    setting.setDimensionType("user");
                    setting.setDimensionTypeName("测试用户");
                    setting.setDimensionTarget(entity.getId());
                    setting.setDimensionTargetName("admin");
                    setting.setState((byte) 1);
                    return setting;
                })
                .as(settingRepository::insert)
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

        Mono<Authentication> authenticationMono = reactiveAuthenticationManager
                .authenticate(Mono.just(new PlainTextUsernamePasswordAuthenticationRequest("admin", "admin")))
                .cache();

        authenticationMono.map(Authentication::getUser)
                .map(User::getName)
                .as(StepVerifier::create)
                .expectNext("admin")
                .verifyComplete();

        authenticationMono.map(autz->autz.hasPermission("test","add"))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        userService.deleteUser(entity.getId())
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        settingRepository.createQuery()
                .where(AuthorizationSettingEntity::getDimensionType,"user")
                .and(AuthorizationSettingEntity::getDimensionTarget,entity.getId())
                .fetch()
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();

    }

}
