package org.hswebframework.web.crud.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryNoPagingOperation;
import org.hswebframework.web.api.crud.entity.QueryOperation;
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

import java.util.Collections;
import java.util.List;

/**
 * 基于{@link SyncRepository}的查询控制器.
 *
 * @param <E> 实体类
 * @param <K> 主键类型
 * @see SyncRepository
 */
public interface QueryController<E, K> {

    @Authorize(ignore = true)
    SyncRepository<E, K> getRepository();

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
    @QueryOperation(summary = "使用GET方式分页动态查询(不返回总数)",
            description = "此操作不返回分页总数,如果需要获取全部数据,请设置参数paging=false")
    default List<E> query(@Parameter(hidden = true) QueryParamEntity query) {
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
    @QueryNoPagingOperation(summary = "使用POST方式分页动态查询(不返回总数)",
            description = "此操作不返回分页总数,如果需要获取全部数据,请设置参数paging=false")
    default List<E> postQuery(@Parameter(hidden = true) @RequestBody QueryParamEntity query) {
        return this.query(query);
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
    default PagerResult<E> queryPager(@Parameter(hidden = true) QueryParamEntity query) {
        if (query.getTotal() != null) {
            return PagerResult
                    .of(query.getTotal(),
                        getRepository()
                                .createQuery()
                                .setParam(query.rePaging(query.getTotal()))
                                .fetch(), query)
                    ;
        }
        int total = getRepository().createQuery().setParam(query.clone()).count();
        if (total == 0) {
            return PagerResult.of(0, Collections.emptyList(), query);
        }
        query.rePaging(total);

        return PagerResult
                .of(total,
                    getRepository()
                            .createQuery()
                            .setParam(query.rePaging(query.getTotal()))
                            .fetch(), query);
    }


    @PostMapping("/_query")
    @QueryAction
    @SuppressWarnings("all")
    @QueryOperation(summary = "使用POST方式分页动态查询")
    default PagerResult<E> postQueryPager(@Parameter(hidden = true) @RequestBody QueryParamEntity query) {
        return queryPager(query);
    }

    @PostMapping("/_count")
    @QueryAction
    @QueryNoPagingOperation(summary = "使用POST方式查询总数")
    default int postCount(@Parameter(hidden = true) @RequestBody QueryParamEntity query) {
         return this.count(query);
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
    @QueryNoPagingOperation(summary = "使用GET方式查询总数")
    default int count(@Parameter(hidden = true) QueryParamEntity query) {
        return getRepository()
                .createQuery()
                .setParam(query)
                .count();
    }

    @GetMapping("/{id:.+}")
    @QueryAction
    @Operation(summary = "根据ID查询")
    default E getById(@PathVariable K id) {
       return getRepository()
                .findById(id)
               .orElseThrow(NotFoundException.NoStackTrace::new);
    }

}
