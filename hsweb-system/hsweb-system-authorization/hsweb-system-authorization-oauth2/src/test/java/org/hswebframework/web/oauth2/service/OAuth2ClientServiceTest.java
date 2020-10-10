package org.hswebframework.web.oauth2.service;

import org.hswebframework.web.oauth2.ReactiveTestApplication;
import org.hswebframework.web.oauth2.entity.OAuth2ClientEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveTestApplication.class)
public class OAuth2ClientServiceTest {

    @Autowired
    OAuth2ClientService clientService;

    @Test
    public void test() {

        OAuth2ClientEntity clientEntity = new OAuth2ClientEntity();
        clientEntity.setId("test");
        clientEntity.setHomeUri("http://hsweb.me");
        clientEntity.setCallbackUri("http://hsweb.me/callback");
        clientEntity.setSecret("test");
        clientEntity.setName("test");
        clientEntity.setUserId("admin");
        clientService.insert(Mono.just(clientEntity))
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

        clientService.findById("test")
                .doOnNext(System.out::println)
                .as(StepVerifier::create)
                .expectNextMatches(client -> {
                    return client.getCreateTime() != null && client.getState() != null;
                }).verifyComplete();

    }

}