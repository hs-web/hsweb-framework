package org.hswebframework.web.authorization.basic.aop;

import org.hswebframework.ezorm.core.CastUtil;
import org.hswebframework.ezorm.core.param.Param;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.web.authorization.*;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.simple.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;
import java.util.function.Function;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class AopAuthorizingControllerTest {

    @Autowired
    public TestController testController;

    @Test
    public void testAccessDeny() {

        SimpleAuthentication authentication = new SimpleAuthentication();

        authentication.setUser(SimpleUser.builder().id("test").username("test").build());
//        authentication.setPermissions(Arrays.asList(SimplePermission.builder().id("test").build()));
        authentication.setPermissions(Collections.emptyList());
        ReactiveAuthenticationHolder.setSupplier(new ReactiveAuthenticationSupplier() {
            @Override
            public Mono<Authentication> get(String userId) {
                return Mono.empty();
            }

            @Override
            public Mono<Authentication> get() {
                return Mono.just(authentication);
            }
        });

        testController.getUser()
                .map(User::getId)
                .onErrorReturn(AccessDenyException.class, "403")
                .as(StepVerifier::create)
                .expectNext("403")
                .verifyComplete();

        testController.getUserAfter()
                .map(User::getId)
                .onErrorReturn(AccessDenyException.class, "403")
                .as(StepVerifier::create)
                .expectNext("403")
                .verifyComplete();
    }

}