package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveQuery;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.hswebframework.web.crud.query.QueryHelper;
import org.hswebframework.web.crud.query.PagerQueryPolicy;
import org.reactivestreams.Publisher;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

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
 * @see org.hswebframework.web.crud.query.QueryHelper
 * @since 4.0
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


    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<E> findById(K id) {
        return getRepository()
            .findById(id);
    }

    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> findById(Collection<K> publisher) {
        return getRepository()
            .findById(publisher);
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<E> findById(Mono<K> publisher) {
        return getRepository()
            .findById(publisher);
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> findById(Flux<K> publisher) {
        return getRepository()
            .findById(publisher);
    }

    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<SaveResult> save(Publisher<E> entityPublisher) {
        return getRepository()
            .save(entityPublisher);
    }

    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<SaveResult> save(E data) {
        return getRepository()
            .save(data);
    }

    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<SaveResult> save(Collection<E> collection) {
        return getRepository()
            .save(collection);
    }

    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> updateById(K id, Mono<E> entityPublisher) {
        return getRepository()
            .updateById(id, entityPublisher);
    }

    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> updateById(K id, E data) {
        return getRepository()
            .updateById(id, Mono.just(data));
    }

    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> insertBatch(Publisher<? extends Collection<E>> entityPublisher) {
        return getRepository()
            .insertBatch(entityPublisher);
    }

    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> insert(Publisher<E> entityPublisher) {
        return getRepository()
            .insert(entityPublisher);
    }

    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> insert(E data) {
        return getRepository()
            .insert(Mono.just(data));
    }

    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> deleteById(Publisher<K> idPublisher) {
        return getRepository()
            .deleteById(idPublisher);
    }

    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> deleteById(K id) {
        return getRepository()
            .deleteById(Mono.just(id));
    }


    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> query(Mono<? extends QueryParamEntity> queryParamMono) {
        return queryParamMono
            .flatMapMany(this::query);
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> query(QueryParamEntity param) {
        return getRepository()
            .createQuery()
            .setParam(param)
            .fetch();
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<PagerResult<E>> queryPager(QueryParamEntity queryParamMono) {
        return queryPager(queryParamMono, Function.identity());
    }

    /**
     * 使用当前订阅解析出的分页策略执行分页查询。
     *
     * <p>策略在订阅期通过 {@link #resolvePagerQueryPolicy(ContextView)} 获取，因此服务实现可以
     * 在保留 Reactor Context 默认行为的同时提供稳定的业务级限制。</p>
     *
     * @param query  查询参数，执行时不会修改原对象
     * @param mapper 结果转换函数
     * @param <T>    结果类型
     * @return 分页查询结果
     * @since 5.0.2
     */
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default <T> Mono<PagerResult<T>> queryPager(QueryParamEntity query, Function<E, T> mapper) {
        return Mono.deferContextual(contextView -> queryPager(
            query,
            mapper,
            resolvePagerQueryPolicy(contextView)));
    }

    /**
     * 使用显式策略执行分页查询。显式策略优先于服务扩展点和 Reactor Context。
     *
     * @param query  查询参数，执行时不会修改原对象
     * @param policy 本次查询使用的非空不可变策略
     * @return 分页查询结果
     * @since 5.0.2
     */
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<PagerResult<E>> queryPager(QueryParamEntity query,
                                            PagerQueryPolicy policy) {
        return queryPager(query, Function.identity(), policy);
    }

    /**
     * 使用显式策略执行分页查询并转换结果。
     *
     * <p>该重载不调用 {@link #resolvePagerQueryPolicy(ContextView)}；策略校验、错误传播、
     * 有界收集和取消语义由 {@link QueryHelper#queryPager(QueryParamEntity, java.util.function.Supplier, Function, PagerQueryPolicy)}
     * 统一处理。</p>
     *
     * @param query  查询参数，执行时不会修改原对象
     * @param mapper 结果转换函数
     * @param policy 本次查询使用的非空不可变策略
     * @param <T>    结果类型
     * @return 分页查询结果
     * @since 5.0.2
     */
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default <T> Mono<PagerResult<T>> queryPager(QueryParamEntity query,
                                                Function<E, T> mapper,
                                                PagerQueryPolicy policy) {
        return QueryHelper.queryPager(query, this::createQuery, mapper, policy);
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default <T> Mono<PagerResult<T>> queryPager(Mono<? extends QueryParamEntity> queryParamMono, Function<E, T> mapper) {
        return queryParamMono
            .cast(QueryParamEntity.class)
            .flatMap(param -> queryPager(param, mapper));
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<PagerResult<E>> queryPager(Mono<? extends QueryParamEntity> queryParamMono) {
        return queryPager(queryParamMono, Function.identity());
    }

    /**
     * 解析当前服务订阅使用的分页策略。
     *
     * <p>框架在每次 {@code queryPager} 订阅时调用本方法。默认实现读取只读 Reactor Context，
     * 未设置时回退到框架稳定默认策略。实现类可以返回服务级不可变策略，但必须保持同步、非阻塞且
     * 返回非空值；抛出的异常会作为当前查询的响应式错误传播，不应在此执行查询或其他副作用。</p>
     *
     * @param contextView 当前订阅的只读 Reactor Context
     * @return 本次订阅使用的非空不可变分页策略
     * @see PagerQueryPolicy#from(ContextView)
     * @since 5.0.2
     */
    default PagerQueryPolicy resolvePagerQueryPolicy(ContextView contextView) {
        return PagerQueryPolicy.from(contextView);
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> count(QueryParamEntity queryParam) {
        return getRepository()
            .createQuery()
            .setParam(queryParam)
            .count();
    }

    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> count(Mono<? extends QueryParamEntity> queryParamMono) {
        return queryParamMono.flatMap(this::count);
    }

}
