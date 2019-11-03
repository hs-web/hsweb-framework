package org.hswebframework.web.crud.web.reactive;

import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.exception.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

public interface ReactiveQueryController<E, K> {

    @Authorize(ignore = true)
    ReactiveRepository<E, K> getRepository();

    @GetMapping("/_query/no-paging")
    @QueryAction
    default Flux<E> query(QueryParamEntity query) {
        return getRepository()
                .createQuery()
                .setParam(query)
                .fetch();
    }

    @PostMapping("/_query/no-paging")
    @QueryAction
    default Flux<E> query(Mono<QueryParamEntity> query) {
        return query.flatMapMany(this::query);
    }

    @GetMapping("/_count")
    @QueryAction
    default Mono<Integer> count(QueryParamEntity query) {
        return getRepository()
                .createQuery()
                .setParam(query)
                .count();
    }

    @GetMapping("/_query")
    @QueryAction
    default Mono<PagerResult<E>> queryPager(QueryParamEntity query) {
        return queryPager(Mono.just(query));
    }

    @PostMapping("/_query")
    @QueryAction
    @SuppressWarnings("all")
    default Mono<PagerResult<E>> queryPager(Mono<QueryParamEntity> query) {
        return  query
                .flatMap(param->{
                    return getRepository().createQuery().setParam(param).count()
                            .flatMap(total->{
                                if (total == 0) {
                                    return Mono.just(PagerResult.empty());
                                }
                                return query
                                        .map(QueryParam::clone)
                                        .flatMap(q -> query(Mono.just(q.rePaging(total))).collectList())
                                        .map(list->PagerResult.of(total,list,param));
                            });
                });
    }

    @PostMapping("/_count")
    @QueryAction
    default Mono<Integer> count(Mono<QueryParamEntity> query) {
        return query.flatMap(this::count);
    }

    @GetMapping("/{id:.+}")
    @QueryAction
    default Mono<E> getById(@PathVariable K id) {
        return getRepository()
                .findById(Mono.just(id))
                .switchIfEmpty(Mono.error(NotFoundException::new));
    }


}
