package org.hswebframework.web.authorization.basic.aop;

import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.annotation.*;
import org.hswebframework.web.authorization.define.Phased;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.crud.web.reactive.ReactiveCrudController;
import org.hswebframework.web.crud.web.reactive.ReactiveQueryController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Resource(id = "test", name = "测试")
public class TestController implements ReactiveCrudController<TestEntity, String> {

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

    @QueryAction
    @FieldDataAccess
    @DimensionDataAccess(ignore = true)
    public Mono<QueryParam> queryUser(QueryParam queryParam) {
        return Mono.just(queryParam);
    }

    @QueryAction
    @FieldDataAccess
    public Mono<QueryParam> queryUser(Mono<QueryParam> queryParam) {
        return queryParam;
    }

    @QueryAction
    @TestDataAccess
    public Mono<QueryParam> queryUserByDimension(Mono<QueryParam> queryParam) {
        return queryParam;
    }

    @SaveAction
    @TestDataAccess
    public Mono<TestEntity> save(Mono<TestEntity> param) {
        return param;
    }

    @Override
    @TestDataAccess(idParamIndex = 0,phased = Phased.after)
    public Mono<Boolean> update(String id, Mono<TestEntity> payload) {
        return ReactiveCrudController.super.update(id, payload);
    }

    @Autowired
    ReactiveRepository<TestEntity, String> reactiveRepository;

    @Override
    public ReactiveRepository<TestEntity, String> getRepository() {
        return reactiveRepository;
    }
}
