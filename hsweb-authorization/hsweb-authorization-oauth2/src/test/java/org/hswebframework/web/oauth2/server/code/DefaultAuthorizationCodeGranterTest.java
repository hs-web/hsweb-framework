package org.hswebframework.web.oauth2.server.code;

import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.authorization.simple.SimpleUser;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.RedisHelper;
import org.hswebframework.web.oauth2.server.impl.RedisAccessTokenManager;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import reactor.test.StepVerifier;

import java.util.Collections;

@Ignore
public class DefaultAuthorizationCodeGranterTest {

    @Test
    public void testRequestToken() {

        StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        context.start();

        DefaultAuthorizationCodeGranter codeGranter = new DefaultAuthorizationCodeGranter(
                new RedisAccessTokenManager(RedisHelper.factory), context, RedisHelper.factory
        );

        OAuth2Client client = new OAuth2Client();
        client.setClientId("test");
        client.setClientSecret("test");
        SimpleAuthentication authentication = new SimpleAuthentication();
        authentication.setUser(SimpleUser
                                       .builder()
                                       .id("test")
                                       .build());

        codeGranter
                .requestCode(new AuthorizationCodeRequest(client, authentication, Collections.emptyMap()))
                .doOnNext(System.out::println)
                .flatMap(response -> codeGranter
                        .requestToken(new AuthorizationCodeTokenRequest(client, Collections.singletonMap("code", response.getCode()))))
                .doOnNext(System.out::println)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

    }

}