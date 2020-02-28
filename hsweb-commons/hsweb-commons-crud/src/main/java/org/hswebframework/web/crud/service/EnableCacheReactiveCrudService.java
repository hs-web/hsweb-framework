package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.cache.ReactiveCache;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface EnableCacheReactiveCrudService<E, K> extends ReactiveCrudService<E, K> {

    ReactiveCache<E> getCache();

    default Mono<E> findById(K id) {
        return this.getCache()
                .mono("id:" + id)
                .onCacheMissResume(ReactiveCrudService.super.findById(Mono.just(id)));
    }

    @Override
    default Mono<E> findById(Mono<K> publisher) {
        return publisher.flatMap(this::findById);
    }

    @Override
    default Mono<Integer> updateById(K id, Mono<E> entityPublisher) {
        return ReactiveCrudService.super.updateById(id, entityPublisher)
                .doFinally(i -> getCache().evict("id:" + id).subscribe());
    }

    @Override
    default Mono<SaveResult> save(Publisher<E> entityPublisher) {
        return ReactiveCrudService.super.save(entityPublisher)
                .doFinally(i -> getCache().clear().subscribe());
    }

    @Override
    default Mono<Integer> insert(Publisher<E> entityPublisher) {
        return ReactiveCrudService.super.insert(entityPublisher)
                .doFinally(i -> getCache().clear().subscribe());
    }

    @Override
    default Mono<Integer> insertBatch(Publisher<? extends Collection<E>> entityPublisher) {
        return ReactiveCrudService.super.insertBatch(entityPublisher)
                .doFinally(i -> getCache().clear().subscribe());
    }

    @Override
    default Mono<Integer> deleteById(Publisher<K> idPublisher) {
        return Flux.from(idPublisher)
                .doOnNext(id -> this.getCache().evict("id:" + id).subscribe())
                .as(ReactiveCrudService.super::deleteById);
    }

    @Override
    default ReactiveUpdate<E> createUpdate() {
        return ReactiveCrudService.super.createUpdate()
                .onExecute((update, s) -> s.doFinally((__) -> getCache().clear().subscribe()));
    }

    @Override
    default ReactiveDelete createDelete() {
        return ReactiveCrudService.super.createDelete()
                .onExecute((update, s) -> s.doFinally((__) -> getCache().clear().subscribe()));
    }
}
