package org.hswebframework.web.crud.web.reactive;

import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.hswebframework.web.exception.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ReactiveServiceQueryController<E, K> {

    @Authorize(ignore = true)
    ReactiveCrudService<E, K> getService();

    @GetMapping("/_query/no-paging")
    @QueryAction
    default Flux<E> query(QueryParamEntity query) {
        return getService()
                .createQuery()
                .setParam(query)
                .fetch();
    }

    @PostMapping("/_query/no-paging")
    @QueryAction
    default Flux<E> query(@RequestBody Mono<QueryParamEntity> query) {
        return query.flatMapMany(this::query);
    }

    @GetMapping("/_count")
    @QueryAction
    default Mono<Integer> count(QueryParamEntity query) {
        return getService()
                .createQuery()
                .setParam(query)
                .count();
    }

    @GetMapping("/_query")
    @QueryAction
    default Mono<PagerResult<E>> queryPager(QueryParamEntity query) {
        return getService().queryPager(query);
    }

    @PostMapping("/_query")
    @QueryAction
    @SuppressWarnings("all")
    default Mono<PagerResult<E>> queryPager(@RequestBody Mono<QueryParamEntity> query) {
        return getService().queryPager(query);
    }

    @PostMapping("/_count")
    @QueryAction
    default Mono<Integer> count(@RequestBody Mono<QueryParamEntity> query) {
        return query.flatMap(this::count);
    }

    @GetMapping("/{id:.+}")
    @QueryAction
    default Mono<E> getById(@PathVariable K id) {
        return getService()
                .findById(Mono.just(id))
                .switchIfEmpty(Mono.error(NotFoundException::new));
    }


}
