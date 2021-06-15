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
     * <pre>
     * Flux&lt;MyEntity&gt; flux=
     *     service
     *     .createQuery()
     *     .where(MyEntity::getName,name)
     *     .in(MyEntity::getState,state1,state2)
     *     .fetch()
     * </pre>
     *
     * @return 动态查询接口
     */
    default ReactiveQuery<E> createQuery() {
        return getRepository().createQuery();
    }

    /**
     * 创建一个DSL动态更新接口,可使用DSL方式进行链式调用来构造动态更新条件.例如:
     * <pre>
     * Mono&lt;Integer&gt; flux=
     *     service
     *     .createUpdate()
     *     .set(entity::getState)
     *     .where(MyEntity::getName,name)
     *     .in(MyEntity::getState,state1,state2)
     *     .execute()
     * </pre>
     *
     * @return 动态更新接口
     */
    default ReactiveUpdate<E> createUpdate() {
        return getRepository().createUpdate();
    }

    /**
     * 创建一个DSL动态删除接口,可使用DSL方式进行链式调用来构造动态删除条件.例如:
     * <pre>
     * Mono&lt;Integer&gt; flux=
     *     service
     *     .createDelete()
     *     .where(MyEntity::getName,name)
     *     .in(MyEntity::getState,state1,state2)
     *     .execute()
     * </pre>
     *
     * @return 动态更新接口
     */
    default ReactiveDelete createDelete() {
        return getRepository().createDelete();
    }


    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Mono<E> findById(K id) {
        return getRepository()
                .findById(id);
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Flux<E> findById(Collection<K> publisher) {
        return getRepository()
                .findById(publisher);
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Mono<E> findById(Mono<K> publisher) {
        return getRepository()
                .findById(publisher);
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Flux<E> findById(Flux<K> publisher) {
        return getRepository()
                .findById(publisher);
    }

    @Transactional(transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Mono<SaveResult> save(Publisher<E> entityPublisher) {
        return getRepository()
                .save(entityPublisher);
    }

    @Transactional(transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Mono<Integer> updateById(K id, Mono<E> entityPublisher) {
        return getRepository()
                .updateById(id, entityPublisher);
    }

    @Transactional(transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Mono<Integer> insertBatch(Publisher<? extends Collection<E>> entityPublisher) {
        return getRepository()
                .insertBatch(entityPublisher);
    }

    @Transactional(transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Mono<Integer> insert(Publisher<E> entityPublisher) {
        return getRepository()
                .insert(entityPublisher);
    }

    @Transactional(transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Mono<Integer> deleteById(Publisher<K> idPublisher) {
        return getRepository()
                .deleteById(idPublisher);
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Flux<E> query(Mono<? extends QueryParamEntity> queryParamMono) {
        return queryParamMono
                .flatMapMany(this::query);
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Flux<E> query(QueryParamEntity param) {
        return getRepository()
                .createQuery()
                .setParam(param)
                .fetch();
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Mono<PagerResult<E>> queryPager(QueryParamEntity queryParamMono) {
        return queryPager(queryParamMono, Function.identity());
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    default <T> Mono<PagerResult<T>> queryPager(QueryParamEntity query, Function<E, T> mapper) {
        if (query.getTotal() != null) {
            return getRepository()
                    .createQuery()
                    .setParam(query.rePaging(query.getTotal()))
                    .fetch()
                    .map(mapper)
                    .collectList()
                    .map(list -> PagerResult.of(query.getTotal(), list, query));
        }
        //并行分页
        if (query.isParallelPager()) {
            return Mono
                    .zip(
                            createQuery().setParam(query).count(),
                            createQuery().setParam(query.clone()).fetch().map(mapper).collectList(),
                            (total, data) -> PagerResult.of(total, data, query)
                    );
        }
        return getRepository()
                .createQuery()
                .setParam(query)
                .count()
                .flatMap(total -> {
                    if (total == 0) {
                        return Mono.just(PagerResult.of(0, new ArrayList<>(), query));
                    }
                    return query(query.clone().rePaging(total))
                            .map(mapper)
                            .collectList()
                            .map(list -> PagerResult.of(total, list, query));
                });
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    default <T> Mono<PagerResult<T>> queryPager(Mono<? extends QueryParamEntity> queryParamMono, Function<E, T> mapper) {
        return queryParamMono
                .cast(QueryParamEntity.class)
                .flatMap(param -> queryPager(param, mapper));
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Mono<PagerResult<E>> queryPager(Mono<? extends QueryParamEntity> queryParamMono) {
        return queryPager(queryParamMono, Function.identity());
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Mono<Integer> count(QueryParamEntity queryParam) {
        return getRepository()
                .createQuery()
                .setParam(queryParam)
                .count();
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    default Mono<Integer> count(Mono<? extends QueryParamEntity> queryParamMono) {
        return queryParamMono.flatMap(this::count);
    }


}
