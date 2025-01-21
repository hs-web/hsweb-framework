package org.hswebframework.web.crud.web.reactive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryNoPagingOperation;
import org.hswebframework.web.api.crud.entity.QueryOperation;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.hswebframework.web.exception.NotFoundException;
import org.hswebframework.web.exception.TraceSourceException;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ReactiveServiceQueryController<E, K> {

    @Authorize(ignore = true)
    ReactiveCrudService<E, K> getService();

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
    @QueryNoPagingOperation(summary = "使用GET方式分页动态查询(不返回总数)",
        description = "此操作不返回分页总数,如果需要获取全部数据,请设置参数paging=false")
    default Flux<E> query(@Parameter(hidden = true) QueryParamEntity query) {
        return getService()
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
    @Operation(summary = "使用POST方式分页动态查询(不返回总数)",
        description = "此操作不返回分页总数,如果需要获取全部数据,请设置参数paging=false")
    default Flux<E> query(@RequestBody Mono<QueryParamEntity> query) {
        return query.flatMapMany(this::query);
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
    @QueryOperation(summary = "使用GET方式分页动态查询")
    default Mono<PagerResult<E>> queryPager(@Parameter(hidden = true) QueryParamEntity query) {
        if (query.getTotal() != null) {
            return getService()
                .createQuery()
                .setParam(query.rePaging(query.getTotal()))
                .fetch()
                .collectList()
                .map(list -> PagerResult.of(query.getTotal(), list, query));
        }
        return getService().queryPager(query);

    }

    /**
     * POST方式动态查询.
     *
     * <pre>
     *     POST /_query
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
    @PostMapping("/_query")
    @QueryAction
    @SuppressWarnings("all")
    @Operation(summary = "使用POST方式分页动态查询")
    default Mono<PagerResult<E>> queryPager(@RequestBody Mono<QueryParamEntity> query) {
        return query.flatMap(q -> queryPager(q));
    }

    /**
     * POST方式动态查询数量.
     *
     * <pre>
     *     POST /_count
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
     * @return 查询结果
     * @see QueryParamEntity
     */
    @PostMapping("/_count")
    @QueryAction
    @Operation(summary = "使用POST方式查询总数")
    default Mono<Integer> count(@RequestBody Mono<QueryParamEntity> query) {
        return getService().count(query);
    }

    /**
     * GET方式动态查询数量.
     *
     * <pre>
     *
     *    GET /_count?pageIndex=0&pageSize=20&where=name is 张三&orderBy=id desc
     *
     * </pre>
     *
     * @param query 查询条件
     * @return 查询结果
     * @see QueryParamEntity
     */
    @GetMapping("/_count")
    @QueryAction
    @QueryNoPagingOperation(summary = "使用GET方式查询总数")
    default Mono<Integer> count(@Parameter(hidden = true) QueryParamEntity query) {
        return Mono.defer(() -> getService().count(query));
    }

    @PostMapping("/_exists")
    @QueryAction
    @Operation(summary = "使用POST方式判断数据是否存在")
    default Mono<Boolean> exists(@RequestBody Mono<QueryParamEntity> query) {
        return query
            .flatMap(param -> getService()
                .createQuery()
                .setParam(param)
                .fetchOne()
                .hasElement());
    }

    /**
     * 使用GET方式判断数据是否存在.
     *
     * <pre>
     *
     *    GET /_exists?where=name is 张三
     *
     * </pre>
     *
     * @param query 查询条件
     * @return 查询结果
     * @see QueryParamEntity
     */
    @GetMapping("/_exists")
    @QueryAction
    @QueryNoPagingOperation(summary = "使用GET方式判断数据是否存在")
    default Mono<Boolean> exists(@Parameter(hidden = true) QueryParamEntity query) {
        return exists(Mono.just(query));
    }

    /**
     * 根据ID查询.
     * <pre>
     * {@code
     *     GET /{id}
     * }
     * </pre>
     *
     * @param id ID
     * @return 结果流
     * @see QueryParamEntity
     */
    @GetMapping("/{id:.+}")
    @QueryAction
    @Operation(summary = "根据ID查询")
    default Mono<E> getById(@PathVariable K id) {
        return getService()
            .findById(id)
            .switchIfEmpty(Mono.error(() -> new NotFoundException
                .NoStackTrace("error.data.find.not_found", id)
                .withSource(ClassUtils.getUserClass(this).getCanonicalName() + ".getById", id)));
    }


}
