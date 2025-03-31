package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.hswebframework.web.cache.ReactiveCache;
import org.hswebframework.web.crud.utils.TransactionUtils;
import org.reactivestreams.Publisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionSynchronization;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface EnableCacheReactiveCrudService<E, K> extends ReactiveCrudService<E, K> {

    ReactiveCache<E> getCache();

    String ALL_DATA_KEY = "@all";

    default Mono<E> findById(K id) {
        return this.getCache().getMono("id:" + id, () -> ReactiveCrudService.super.findById(id));
    }

    @Override
    default Mono<E> findById(Mono<K> publisher) {
        return publisher.flatMap(this::findById);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> updateById(K id, E data) {
        return updateById(id, Mono.just(data));
    }

    @Override
    default Mono<Integer> updateById(K id, Mono<E> entityPublisher) {
        return registerClearCache(Collections.singleton("id:" + id))
                .then(ReactiveCrudService.super.updateById(id, entityPublisher));
    }

    @Override
    default Mono<SaveResult> save(Collection<E> collection) {
        return registerClearCache()
                .then(ReactiveCrudService.super.save(collection));
    }

    @Override
    default Mono<SaveResult> save(E data) {
        return registerClearCache()
                .then(ReactiveCrudService.super.save(data));
    }

    @Override
    default Mono<SaveResult> save(Publisher<E> entityPublisher) {
        return registerClearCache()
                .then(ReactiveCrudService.super.save(entityPublisher));
    }

    @Override
    default Mono<Integer> insert(E data) {
        return registerClearCache()
                .then(ReactiveCrudService.super.insert(data));
    }

    @Override
    default Mono<Integer> insert(Publisher<E> entityPublisher) {
        return registerClearCache()
                .then(ReactiveCrudService.super.insert(entityPublisher));
    }

    @Override
    default Mono<Integer> insertBatch(Publisher<? extends Collection<E>> entityPublisher) {
        return registerClearCache()
                .then(ReactiveCrudService.super.insertBatch(entityPublisher));
    }

    default Mono<Void> registerClearCache() {
        return TransactionUtils.registerSynchronization(new TransactionSynchronization() {
            @Override
            @Nonnull
            public Mono<Void> afterCommit() {
                return getCache().clear();
            }
        }, TransactionSynchronization::afterCommit);
    }

    default Mono<Void> registerClearCache(Collection<?> keys) {
        return TransactionUtils.registerSynchronization(new TransactionSynchronization() {
            @Override
            @Nonnull
            public Mono<Void> afterCommit() {
                Set<Object> set = new HashSet<>(keys);
                //同步删除全量数据的缓存
                set.add(ALL_DATA_KEY);
                return getCache().evictAll(set);
            }
        }, TransactionSynchronization::afterCommit);
    }


    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> deleteById(K id) {
        return deleteById(Mono.just(id));
    }

    @Override
    default Mono<Integer> deleteById(Publisher<K> idPublisher) {
        Flux<K> cache = Flux.from(idPublisher).cache();
        return cache
            .map(id -> "id:" + id)
            .collectList()
            .flatMap(this::registerClearCache)
            .then(ReactiveCrudService.super.deleteById(cache));
    }

    @Override
    default ReactiveUpdate<E> createUpdate() {
        return ReactiveCrudService.super
                .createUpdate()
                .onExecute((update, s) -> s.flatMap(i -> {
                    if (i > 0) {
                        return getCache().clear().thenReturn(i);
                    }
                    return Mono.just(i);
                }));
    }

    @Override
    default ReactiveDelete createDelete() {
        return ReactiveCrudService.super
                .createDelete()
                .onExecute((update, s) -> s.flatMap(i -> {
                    if (i > 0) {
                        return getCache().clear().thenReturn(i);
                    }
                    return Mono.just(i);
                }));
    }
}
