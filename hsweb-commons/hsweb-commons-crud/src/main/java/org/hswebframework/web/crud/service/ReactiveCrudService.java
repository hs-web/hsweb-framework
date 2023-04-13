package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveQuery;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.reactivestreams.Publisher;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

/**
 * 响应式增删改查通用服务类,增删改查,实现此接口.
 * 利用{@link ReactiveRepository}来实现.
 *
 * @param <E> 实体类类型
 * @param <K> 主键类型
 * @see ReactiveRepository
 * @see GenericReactiveCrudService
 * @see GenericReactiveTreeSupportCrudService
 * @see EnableCacheReactiveCrudService
 */
public interface ReactiveCrudService<E, K> {

    /**
     * @return 响应式实体操作仓库
     */
    ReactiveRepository<E, K> getRepository();

    /**
     * 创建一个DSL的动态查询接口,可使用DSL方式进行链式调用来构造动态查询条件.例如:
     * <pre>{@code
     * Flux<MyEntity> flux = service
     *     .createQuery()
     *     .where(MyEntity::getName,name)
     *     .in(MyEntity::getState,state1,state2)
     *     .fetch()
     * }
     * </pre>
     *
     * @return 动态查询接口
     */
    default ReactiveQuery<E> createQuery() {
        return getRepository().createQuery();
    }

    /**
     * 创建一个DSL动态更新接口,可使用DSL方式进行链式调用来构造动态更新条件.例如:
     * <pre>{@code
     * Mono<Integer> result = service
     *     .createUpdate()
     *     .set(entity::getState)
     *     .where(MyEntity::getName,name)
     *     .in(MyEntity::getState,state1,state2)
     *     .execute()
     *     }
     * </pre>
     *
     * @return 动态更新接口
     */
    default ReactiveUpdate<E> createUpdate() {
        return getRepository().createUpdate();
    }

    /**
     * 创建一个DSL动态删除接口,可使用DSL方式进行链式调用来构造动态删除条件.例如:
     * <pre>{@code
     * Mono<Integer> result = service
     *     .createDelete()
     *     .where(MyEntity::getName,name)
     *     .in(MyEntity::getState,state1,state2)
     *     .execute()
     * }
     * </pre>
     *
     * @return 动态更新接口
     */
    default ReactiveDelete createDelete() {
        return getRepository().createDelete();
    }


    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<E> findById(K id) {
        return getRepository()
                .findById(id);
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> findById(Collection<K> publisher) {
        return getRepository()
                .findById(publisher);
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<E> findById(Mono<K> publisher) {
        return getRepository()
                .findById(publisher);
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> findById(Flux<K> publisher) {
        return getRepository()
                .findById(publisher);
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<SaveResult> save(Publisher<E> entityPublisher) {
        return getRepository()
                .save(entityPublisher);
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<SaveResult> save(E data) {
        return getRepository()
                .save(data);
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<SaveResult> save(Collection<E> collection) {
        return getRepository()
                .save(collection);
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> updateById(K id, Mono<E> entityPublisher) {
        return getRepository()
                .updateById(id, entityPublisher);
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> updateById(K id, E data) {
        return getRepository()
                .updateById(id, Mono.just(data));
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> insertBatch(Publisher<? extends Collection<E>> entityPublisher) {
        return getRepository()
                .insertBatch(entityPublisher);
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> insert(Publisher<E> entityPublisher) {
        return getRepository()
                .insert(entityPublisher);
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> insert(E data) {
        return getRepository()
                .insert(Mono.just(data));
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> deleteById(Publisher<K> idPublisher) {
        return getRepository()
                .deleteById(idPublisher);
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> deleteById(K id) {
        return getRepository()
                .deleteById(Mono.just(id));
    }


    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> query(Mono<? extends QueryParamEntity> queryParamMono) {
        return queryParamMono
                .flatMapMany(this::query);
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> query(QueryParamEntity param) {
        return getRepository()
                .createQuery()
                .setParam(param)
                .fetch();
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<PagerResult<E>> queryPager(QueryParamEntity queryParamMono) {
        return queryPager(queryParamMono, Function.identity());
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default <T> Mono<PagerResult<T>> queryPager(QueryParamEntity query, Function<E, T> mapper) {
        //如果查询参数指定了总数,表示不需要再进行count操作.
        //建议前端在使用分页查询时,切换下一页时,将第一次查询到total结果传入查询参数,可以提升查询性能.
        if (query.getTotal() != null) {
            return getRepository()
                    .createQuery()
                    .setParam(query.rePaging(query.getTotal()))
                    .fetch()
                    .map(mapper)
                    .collectList()
                    .map(list -> PagerResult.of(query.getTotal(), list, query));
        }
        //并行分页,更快,所在页码无数据时,会返回空list.
        if (query.isParallelPager()) {
            return Mono
                    .zip(
                            createQuery().setParam(query.clone()).count(),
                            createQuery().setParam(query.clone()).fetch().map(mapper).collectList(),
                            (total, data) -> PagerResult.of(total, data, query)
                    );
        }
        return getRepository()
                .createQuery()
                .setParam(query.clone())
                .count()
                .flatMap(total -> {
                    if (total == 0) {
                        return Mono.just(PagerResult.of(0, new ArrayList<>(), query));
                    }
                    //查询前根据数据总数进行重新分页:要跳转的页码没有数据则跳转到最后一页
                    QueryParamEntity rePagingQuery = query.clone().rePaging(total);
                    return query(rePagingQuery)
                            .map(mapper)
                            .collectList()
                            .map(list -> PagerResult.of(total, list, rePagingQuery));
                });
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default <T> Mono<PagerResult<T>> queryPager(Mono<? extends QueryParamEntity> queryParamMono, Function<E, T> mapper) {
        return queryParamMono
                .cast(QueryParamEntity.class)
                .flatMap(param -> queryPager(param, mapper));
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<PagerResult<E>> queryPager(Mono<? extends QueryParamEntity> queryParamMono) {
        return queryPager(queryParamMono, Function.identity());
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> count(QueryParamEntity queryParam) {
        return getRepository()
                .createQuery()
                .setParam(queryParam)
                .count();
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> count(Mono<? extends QueryParamEntity> queryParamMono) {
        return queryParamMono.flatMap(this::count);
    }


}
