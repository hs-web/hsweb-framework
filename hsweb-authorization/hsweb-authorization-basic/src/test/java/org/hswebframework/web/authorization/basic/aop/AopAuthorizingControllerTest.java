package org.hswebframework.web.authorization.basic.aop;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.ReactiveAuthenticationHolder;
import org.hswebframework.web.authorization.ReactiveAuthenticationSupplier;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.basic.web.ReactiveUserTokenParser;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.authorization.simple.SimplePermission;
import org.hswebframework.web.authorization.simple.SimpleUser;
import org.hswebframework.web.authorization.token.ParsedToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

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
        ReactiveAuthenticationHolder.addSupplier(new ReactiveAuthenticationSupplier() {
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