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

import java.util.Collection;
import java.util.function.Function;

public interface ReactiveCrudService<E, K> {

    ReactiveRepository<E, K> getRepository();

    default ReactiveQuery<E> createQuery() {
        return getRepository().createQuery();
    }

    default ReactiveUpdate<E> createUpdate() {
        return getRepository().createUpdate();
    }

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
                        return Mono.just(PagerResult.empty());
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
