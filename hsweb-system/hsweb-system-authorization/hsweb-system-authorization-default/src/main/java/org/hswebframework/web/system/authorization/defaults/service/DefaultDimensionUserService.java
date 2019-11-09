package org.hswebframework.web.system.authorization.defaults.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;
import org.hswebframework.web.system.authorization.api.event.ClearUserAuthorizationCacheEvent;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.stream.Collectors;

public class DefaultDimensionUserService extends GenericReactiveCrudService<DimensionUserEntity, String> {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Mono<SaveResult> save(Publisher<DimensionUserEntity> entityPublisher) {
        return Flux.from(entityPublisher)
                .doOnNext(entity -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.of(entity.getUserId())))
                .as(super::save);
    }

    @Override
    public Mono<Integer> updateById(String id, Mono<DimensionUserEntity> entityPublisher) {
        return entityPublisher
                .doOnNext(entity -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.of(entity.getUserId())))
                .as(e -> super.updateById(id, e));
    }

    @Override
    public Mono<Integer> insert(Publisher<DimensionUserEntity> entityPublisher) {
        return Flux.from(entityPublisher)
                .doOnNext(entity -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.of(entity.getUserId())))
                .as(super::insert);
    }

    @Override
    public Mono<Integer> insertBatch(Publisher<? extends Collection<DimensionUserEntity>> entityPublisher) {
        return Flux.from(entityPublisher)
                .doOnNext(entity -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.of(entity
                        .stream()
                        .map(DimensionUserEntity::getUserId)
                        .collect(Collectors.toSet()))))
                .as(super::insertBatch);
    }

    @Override
    public Mono<Integer> deleteById(Publisher<String> idPublisher) {
        return findById(Flux.from(idPublisher))
                .doOnNext(entity -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.of(entity.getUserId())))
                .map(DimensionUserEntity::getId)
                .as(super::deleteById);
    }

    @Override
    @SuppressWarnings("all")
    public ReactiveUpdate<DimensionUserEntity> createUpdate() {
        return super.createUpdate()
                .onExecute((update, r) -> r.doOnSuccess(i -> {
                    createQuery()
                            .select(DimensionUserEntity::getUserId)
                            .setParam(update.toQueryParam())
                            .fetch()
                            .map(DimensionUserEntity::getUserId)
                            .collectList()
                            .map(ClearUserAuthorizationCacheEvent::of)
                            .subscribe();
                }));
    }

    @Override
    @SuppressWarnings("all")
    public ReactiveDelete createDelete() {
        return super.createDelete()
                .onExecute((delete, r) -> r.doOnSuccess(i -> {
                    createQuery()
                            .select(DimensionUserEntity::getUserId)
                            .setParam(delete.toQueryParam())
                            .fetch()
                            .map(DimensionUserEntity::getUserId)
                            .collectList()
                            .map(ClearUserAuthorizationCacheEvent::of)
                            .subscribe();
                }));
    }
}
