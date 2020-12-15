package org.hswebframework.web.system.authorization.defaults.service;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.exception.DuplicateKeyException;
import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;
import org.hswebframework.web.system.authorization.api.event.ClearUserAuthorizationCacheEvent;
import org.hswebframework.web.system.authorization.api.event.UserDeletedEvent;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
public class DefaultDimensionUserService extends GenericReactiveCrudService<DimensionUserEntity, String> {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    public void handleUserDeleteEntity(UserDeletedEvent event) {
        createDelete()
                .where(DimensionUserEntity::getUserId, event.getUser().getId())
                .execute()
                .subscribe(i -> log.debug("user deleted,clear user dimension!"));
    }

    @Override
    public Mono<SaveResult> save(Publisher<DimensionUserEntity> entityPublisher) {
        return Flux.from(entityPublisher)
                   .doOnNext(DimensionUserEntity::generateId)
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
                   .doOnNext(DimensionUserEntity::generateId)
                   .doOnNext(entity -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.of(entity.getUserId())))
                   .as(super::insert)
                   .onErrorMap(DuplicateKeyException.class, (err) -> new BusinessException("重复的绑定请求"));
    }

    @Override
    public Mono<Integer> insertBatch(Publisher<? extends Collection<DimensionUserEntity>> entityPublisher) {
        return Flux.from(entityPublisher)
                   .doOnNext(entity -> eventPublisher
                           .publishEvent(ClearUserAuthorizationCacheEvent
                                                 .of(entity.stream()
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
                    .onExecute((update, r) -> r
                            .doOnSuccess(i -> {
                                this.createQuery()
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
                        this.createQuery()
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
