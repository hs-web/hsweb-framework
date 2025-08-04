package org.hswebframework.web.authorization.context;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationHolder;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationThreadLocalAccessorTest {


    @Test
    void testReadFromReactive() {

        Hooks.enableAutomaticContextPropagation();

        Authentication auth = new SimpleAuthentication();

        Authentication auth2 = AuthenticationHolder.executeWith(
            auth,
            () -> Mono
                .fromCallable(() -> {
                    // cross context
                    return Authentication.current().orElse(null);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .contextWrite(c->c)
                .block());

        assertEquals(auth, auth2);


    }
}