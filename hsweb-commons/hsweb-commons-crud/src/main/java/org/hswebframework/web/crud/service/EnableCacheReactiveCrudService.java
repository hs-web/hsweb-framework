package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.cache.ReactiveCache;
import org.hswebframework.web.crud.utils.TransactionUtils;
import org.reactivestreams.Publisher;
import org.springframework.transaction.reactive.TransactionSynchronization;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Function;

public interface EnableCacheReactiveCrudService<E, K> extends ReactiveCrudService<E, K> {

    ReactiveCache<E> getCache();

    default Mono<E> findById(K id) {
        return this
                .getCache()
                .getMono("id:" + id, () -> ReactiveCrudService.super.findById(id));
    }

    @Override
    default Mono<E> findById(Mono<K> publisher) {
        return publisher.flatMap(this::findById);
    }

    @Override
    default Mono<Integer> updateById(K id, Mono<E> entityPublisher) {
        return registerClearCache("id:" + id)
                .then(ReactiveCrudService.super.updateById(id, entityPublisher));
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

    default Mono<Void> registerClearCache(String key) {
        return TransactionUtils.registerSynchronization(new TransactionSynchronization() {
            @Override
            @Nonnull
            public Mono<Void> afterCommit() {
                return getCache().evict(key);
            }
        }, TransactionSynchronization::afterCommit);
    }


    @Override
    default Mono<Integer> deleteById(Publisher<K> idPublisher) {
        Flux<K> cache = Flux.from(idPublisher).cache();
        return TransactionUtils
                .registerSynchronization(new TransactionSynchronization() {
                    @Override
                    @Nonnull
                    public Mono<Void> afterCommit() {
                        return cache
                                .flatMap(id -> getCache().evict("id:" + id))
                                .then();
                    }
                }, TransactionSynchronization::afterCommit)
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
