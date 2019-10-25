package org.hswebframework.web.system.authorization.defaults.service.reactive;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest;
import org.hswebframework.web.system.authorization.api.entity.ActionEntity;
import org.hswebframework.web.system.authorization.api.entity.AuthorizationSettingEntity;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;

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

    }

}