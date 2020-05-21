package org.hswebframework.web.crud.web.reactive;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.exception.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 基于{@link ReactiveRepository}的响应式查询控制器.
 *
 * @param <E> 实体类
 * @param <K> 主键类型
 * @see ReactiveRepository
 */
public interface ReactiveQueryController<E, K> {

    @Authorize(ignore = true)
    ReactiveRepository<E, K> getRepository();

    /**
     * 查询,但是不返回分页结果.
     *
     * <pre>
     *     GET /_query/no-paging?pageIndex=0&pageSize=20&where=name is 张三&orderBy=id desc
     * </pre>
     *
     * @param query 动态查询条件
     * @return 结果流
     * @see QueryParamEntity
     */
    @GetMapping("/_query/no-paging")
    @QueryAction
    default Flux<E> query(QueryParamEntity query) {
        return getRepository()
                .createQuery()
                .setParam(query)
                .fetch();
    }

    /**
     * POST方式查询.不返回分页结果
     *
     * <pre>
     *     POST /_query/no-paging
     *
     *     {
     *         "pageIndex":0,
     *         "pageSize":20,
     *         "where":"name like 张%", //放心使用,没有SQL注入
     *         "orderBy":"id desc",
     *         "terms":[ //高级条件
     *             {
     *                 "column":"name",
     *                 "termType":"like",
     *                 "value":"张%"
     *             }
     *         ]
     *     }
     * </pre>
     *
     * @param query 查询条件
     * @return 结果流
     * @see QueryParamEntity
     */
    @PostMapping("/_query/no-paging")
    @QueryAction
    default Flux<E> query(@RequestBody Mono<QueryParamEntity> query) {
        return query.flatMapMany(this::query);
    }

    /**
     * 统计查询
     *
     * <pre>
     *     GET /_count
     * </pre>
     *
     * @param query 查询条件
     * @return 统计结果
     */
    @GetMapping("/_count")
    @QueryAction
    default Mono<Integer> count(QueryParamEntity query) {
        return getRepository()
                .createQuery()
                .setParam(query)
                .count();
    }

    /**
     * GET方式分页查询
     *
     * <pre>
     *    GET /_query/no-paging?pageIndex=0&pageSize=20&where=name is 张三&orderBy=id desc
     * </pre>
     *
     * @param query 查询条件
     * @return 分页查询结果
     * @see PagerResult
     */
    @GetMapping("/_query")
    @QueryAction
    default Mono<PagerResult<E>> queryPager(QueryParamEntity query) {
        if (query.getTotal() != null) {
            return getRepository()
                    .createQuery()
                    .setParam(query.rePaging(query.getTotal()))
                    .fetch()
                    .collectList()
                    .map(list -> PagerResult.of(query.getTotal(), list, query));
        }
        return getRepository()
                .createQuery()
                .setParam(query)
                .count()
                .flatMap(total -> {
                    if (total == 0) {
                        return Mono.just(PagerResult.empty());
                    }
                    return query(query.clone().rePaging(total))
                            .collectList()
                            .map(list -> PagerResult.of(total, list, query));
                });
    }


    @PostMapping("/_query")
    @QueryAction
    @SuppressWarnings("all")
    default Mono<PagerResult<E>> queryPager(@RequestBody Mono<QueryParamEntity> query) {
        return query.flatMap(q -> queryPager(q));
    }

    @PostMapping("/_count")
    @QueryAction
    default Mono<Integer> count(@RequestBody Mono<QueryParamEntity> query) {
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
