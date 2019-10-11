package org.hswebframework.web.authorization.basic.aop;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/test")
public class FluxTestController {

    @GetMapping
    public Mono<Authentication> getUser() {

        return Authentication
                .currentReactive()
                .switchIfEmpty(Mono.error(UnAuthorizedException::new));
    }
}