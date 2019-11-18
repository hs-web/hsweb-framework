package org.hswebframework.web.authorization.basic.aop;

import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.annotation.*;
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

    @QueryAction(dataAccess = @DataAccess(type = @DataAccessType(id= DataAccessConfig.DefaultType.DENY_FIELDS,name = "禁止访问字段")))
    public Mono<QueryParam> queryUser(QueryParam queryParam) {
        return Mono.just(queryParam);
    }

    @QueryAction(dataAccess = @DataAccess(type = @DataAccessType(id= DataAccessConfig.DefaultType.DENY_FIELDS,name = "禁止访问字段")))
    public Mono<QueryParam> queryUser(Mono<QueryParam> queryParam) {
        return queryParam;
    }


}
