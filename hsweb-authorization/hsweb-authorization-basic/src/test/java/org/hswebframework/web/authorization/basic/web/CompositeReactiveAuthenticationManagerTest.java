package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.*;
import org.hswebframework.web.authorization.simple.CompositeReactiveAuthenticationManager;
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.authorization.simple.SimpleUser;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;


public class CompositeReactiveAuthenticationManagerTest {

    @Test
    public void test() {
        CompositeReactiveAuthenticationManager manager = new CompositeReactiveAuthenticationManager(
                Arrays.asList(
                        new ReactiveAuthenticationManagerProvider() {
                            @Override
                            public Mono<Authentication> authenticate(Mono<AuthenticationRequest> request) {
                                return Mono.error(new IllegalArgumentException("密码错误"));
                            }

                            @Override
                            public Mono<Authentication> getByUserId(String userId) {
                                return Mono.empty();
                            }
                        },
                        new ReactiveAuthenticationManagerProvider() {
                            @Override
                            public Mono<Authentication> authenticate(Mono<AuthenticationRequest> request) {
                                SimpleAuthentication authentication = new SimpleAuthentication();
                                authentication.setUser(SimpleUser.builder().id("test").build());

                                return Mono.just(authentication);
                            }

                            @Override
                            public Mono<Authentication> getByUserId(String userId) {
                                return Mono.empty();
                            }
                        }
                )
        );

        manager.authenticate(Mono.just(new PlainTextUsernamePasswordAuthenticationRequest()))
                .map(Authentication::getUser)
                .map(User::getId)
                .as(StepVerifier::create)
                .expectNext("test")
                .verifyComplete();
    }
}