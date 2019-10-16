package org.hswebframework.web.authorization.basic.aop;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.define.Phased;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Resource(id = "test", name = "测试")
public class TestController {

    @QueryAction
    public Mono<User> getUser() {
        return Authentication.currentReactive()
                .switchIfEmpty(Mono.error(new UnAuthorizedException()))
                .map(Authentication::getUser);
    }

    @QueryAction
    public Mono<User> getUserAfter() {
        return Authentication.currentReactive()
                .switchIfEmpty(Mono.error(new UnAuthorizedException()))
                .map(Authentication::getUser);
    }
}
