package org.hswebframework.web.system.authorization.defaults.service;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.exception.DuplicateKeyException;
import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.event.AsyncEvent;
import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;
import org.hswebframework.web.system.authorization.api.event.ClearUserAuthorizationCacheEvent;
import org.hswebframework.web.system.authorization.api.event.DimensionBindEvent;
import org.hswebframework.web.system.authorization.api.event.DimensionUnbindEvent;
import org.hswebframework.web.system.authorization.api.event.UserDeletedEvent;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.Function3;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class DefaultDimensionUserService extends GenericReactiveCrudService<DimensionUserEntity, String> {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    public void handleUserDeleteEntity(UserDeletedEvent event) {
        event.async(this.createDelete()
                        .where(DimensionUserEntity::getUserId, event.getUser().getId())
                        .execute()
                        .doOnSuccess(i -> log.debug("user deleted,clear user dimension!"))
        );
    }

    @Override
    public Mono<SaveResult> save(Publisher<DimensionUserEntity> entityPublisher) {
        return this
                .publishEvent(entityPublisher, DimensionBindEvent::new)
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
        return this
                .publishEvent(entityPublisher, DimensionBindEvent::new)
                .as(super::insert)
                .onErrorMap(DuplicateKeyException.class, (err) -> new BusinessException("重复的绑定请求"));
    }

    @Override
    public Mono<Integer> insertBatch(Publisher<? extends Collection<DimensionUserEntity>> entityPublisher) {

        Flux<? extends Collection<DimensionUserEntity>> cache = Flux.from(entityPublisher).cache();

        return this
                .publishEvent(cache.flatMapIterable(Function.identity()), DimensionBindEvent::new)
                .then(super.insertBatch(cache));
    }

    private Flux<DimensionUserEntity> publishEvent(Publisher<DimensionUserEntity> stream,
                                                   Function3<String, String, List<String>, AsyncEvent> event) {
        Flux<DimensionUserEntity> cache = Flux.from(stream).doOnNext(DimensionUserEntity::generateId).cache();
        return cache
                .groupBy(DimensionUserEntity::getDimensionTypeId)
                .flatMap(typeGroup -> {
                    String type = typeGroup.key();
                    return typeGroup
                            .groupBy(DimensionUserEntity::getDimensionId)
                            .flatMap(dimensionIdGroup -> {
                                String dimensionId = dimensionIdGroup.key();

                                return dimensionIdGroup
                                        .map(DimensionUserEntity::getUserId)
                                        .collectList()
                                        .flatMap(userIdList -> {
                                            eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.of(userIdList));
                                            return event.apply(type, dimensionId, userIdList).publish(eventPublisher);
                                        });
                            });
                })
                .thenMany(cache);
    }

    @Override
    @SuppressWarnings("all")
    public ReactiveUpdate<DimensionUserEntity> createUpdate() {
        return super
                .createUpdate()
                .onExecute((update, r) -> r
                        .flatMap(result -> this
                                .createQuery()
                                .select(DimensionUserEntity::getUserId)
                                .setParam(update.toQueryParam())
                                .fetch()
                                .map(DimensionUserEntity::getUserId)
                                .distinct()
                                .collectList()
                                .map(ClearUserAuthorizationCacheEvent::of)
                                .doOnNext(eventPublisher::publishEvent)
                                .thenReturn(result)
                        )
                );
    }

    @Override
    @SuppressWarnings("all")
    public ReactiveDelete createDelete() {
        return super
                .createDelete()
                .onExecute((delete, r) -> this
                        .publishEvent(this.createQuery()
                                          .select(DimensionUserEntity::getUserId)
                                          .setParam(delete.toQueryParam())
                                          .fetch(),
                                      DimensionUnbindEvent::new
                        ).then(r)
                );
    }
}
