package org.hswebframework.web.system.authorization.defaults.service.reactive;

import com.google.common.cache.CacheBuilder;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.ReactiveAuthenticationInitializeService;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest;
import org.hswebframework.web.cache.ReactiveCacheManager;
import org.hswebframework.web.cache.supports.GuavaReactiveCacheManager;
import org.hswebframework.web.system.authorization.api.entity.ActionEntity;
import org.hswebframework.web.system.authorization.api.entity.AuthorizationSettingEntity;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.event.ClearUserAuthorizationCacheEvent;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.hswebframework.web.system.authorization.defaults.service.DefaultReactiveAuthenticationManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    public void rejectsUnavailableUsersWithoutCache() {
        String userId = "state-check";
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setStatus((byte) 0);
        ReactiveUserService users = mock(ReactiveUserService.class);
        ReactiveAuthenticationInitializeService initializer = mock(ReactiveAuthenticationInitializeService.class);
        Authentication authentication = mock(Authentication.class);
        when(users.findById(userId)).thenReturn(Mono.just(user));
        when(users.findById("missing")).thenReturn(Mono.empty());
        when(initializer.initUserAuthorization(userId)).thenReturn(Mono.just(authentication));
        DefaultReactiveAuthenticationManager manager = createManager(users, initializer, null);

        manager.getByUserId("missing")
               .as(StepVerifier::create)
               .verifyComplete();
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .verifyComplete();
        user.setStatus(null);
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .verifyComplete();
        verifyNoInteractions(initializer);

        user.setStatus((byte) 1);
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .expectNext(authentication)
               .verifyComplete();
        verify(initializer).initUserAuthorization(userId);
    }

    @Test
    public void reloadsEnabledUsersAfterCacheEvictionWithoutReadingWarmUsers() {
        String userId = "cached-state-check";
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setStatus((byte) 1);
        AtomicInteger userReads = new AtomicInteger();
        AtomicInteger initializations = new AtomicInteger();
        ReactiveUserService users = mock(ReactiveUserService.class);
        ReactiveAuthenticationInitializeService initializer = mock(ReactiveAuthenticationInitializeService.class);
        Authentication authentication = mock(Authentication.class);
        when(users.findById(userId)).thenReturn(Mono.defer(() -> {
            userReads.incrementAndGet();
            return Mono.just(user);
        }));
        when(users.findById("missing")).thenReturn(Mono.empty());
        when(initializer.initUserAuthorization(userId)).thenReturn(Mono.defer(() -> {
            initializations.incrementAndGet();
            return Mono.just(authentication);
        }));
        DefaultReactiveAuthenticationManager manager = createManager(
            users, initializer, new GuavaReactiveCacheManager(CacheBuilder.newBuilder()));

        for (int i = 0; i < 3; i++) {
            manager.getByUserId(userId)
                   .as(StepVerifier::create)
                   .expectNext(authentication)
                   .verifyComplete();
        }
        Assert.assertEquals(1, userReads.get());
        Assert.assertEquals(1, initializations.get());

        user.setStatus((byte) 0);
        clearCache(manager, userId);
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .verifyComplete();
        Assert.assertEquals(2, userReads.get());
        Assert.assertEquals(1, initializations.get());

        user.setStatus((byte) 1);
        clearCache(manager, userId);
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .expectNext(authentication)
               .verifyComplete();
        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .expectNext(authentication)
               .verifyComplete();
        Assert.assertEquals(3, userReads.get());
        Assert.assertEquals(2, initializations.get());

        manager.getByUserId("missing")
               .as(StepVerifier::create)
               .verifyComplete();
        verify(initializer, never()).initUserAuthorization("missing");
    }

    @Test
    public void propagatesUserLookupErrorsWithoutCache() {
        String userId = "lookup-error";
        IllegalStateException failure = new IllegalStateException("database unavailable");
        ReactiveUserService users = mock(ReactiveUserService.class);
        ReactiveAuthenticationInitializeService initializer = mock(ReactiveAuthenticationInitializeService.class);
        when(users.findById(userId)).thenReturn(Mono.error(failure));
        DefaultReactiveAuthenticationManager manager = createManager(users, initializer, null);

        manager.getByUserId(userId)
               .as(StepVerifier::create)
               .expectErrorSatisfies(error -> Assert.assertSame(failure, error))
               .verify();
        verifyNoInteractions(initializer);
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
