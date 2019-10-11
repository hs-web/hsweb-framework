package org.hswebframework.web.system.authorization.defaults.service.reactive;

import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveTestApplication.class)
public class DefaultReactiveUserServiceTest {

    @Autowired
    private ReactiveUserService userService;

    @Test
    public void testCrud() {
        UserEntity userEntity = userService.newUserInstance().blockOptional().orElseThrow(NullPointerException::new);
        userEntity.setName("test");
        userEntity.setUsername("admin");
        userEntity.setPassword("admin");

        userService.saveUser(Mono.just(userEntity))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        Assert.assertNotNull(userEntity.getId());

        userEntity.setUsername("admin2");
        userEntity.setPassword("admin2");
        userService.saveUser(Mono.just(userEntity))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        userService.changeState(Mono.just(userEntity.getId()), (byte) 1)
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

        userService.changePassword(userEntity.getId(), "admin2", "admin")
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

         userService.findByUsernameAndPassword("admin", "admin")
                .as(StepVerifier::create)
                 .expectNextCount(1)
                .verifyComplete();


    }

}