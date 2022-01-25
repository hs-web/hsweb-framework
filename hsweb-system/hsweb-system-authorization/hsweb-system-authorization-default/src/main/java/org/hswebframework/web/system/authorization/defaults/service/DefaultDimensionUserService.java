package org.hswebframework.web.system.authorization.defaults.service;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.exception.DuplicateKeyException;
import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.crud.events.EntityCreatedEvent;
import org.hswebframework.web.crud.events.EntityDeletedEvent;
import org.hswebframework.web.crud.events.EntityModifyEvent;
import org.hswebframework.web.crud.events.EntitySavedEvent;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.event.AsyncEvent;
import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.system.authorization.api.entity.DimensionEntity;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Slf4j
public class DefaultDimensionUserService extends GenericReactiveCrudService<DimensionUserEntity, String> {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    //处理用户被删除时,同步删除维度绑定信息
    @EventListener
    public void handleUserDeleteEntity(UserDeletedEvent event) {
        event.async(this.createDelete()
                        .where(DimensionUserEntity::getUserId, event.getUser().getId())
                        .execute()
                        .doOnSuccess(i -> log.debug("user deleted,clear user dimension!"))
        );
    }

    //转发保存维度信息到DimensionBindEvent事件,并清空权限缓存
    @EventListener
    public void dispatchDimensionBind(EntitySavedEvent<DimensionUserEntity> event) {
        event.async(
                this.publishEvent(Flux.fromIterable(event.getEntity()), DimensionBindEvent::new)
                    .then(
                            this.clearUserCache(event.getEntity())
                    )
        );
    }

    //新增绑定时转发DimensionBindEvent并清空用户权限信息
    @EventListener
    public void dispatchDimensionBind(EntityCreatedEvent<DimensionUserEntity> event) {
        event.async(
                this.publishEvent(Flux.fromIterable(event.getEntity()), DimensionBindEvent::new)
                    .then(
                            this.clearUserCache(event.getEntity())
                    )
        );
    }

    //删除绑定时转发DimensionUnbindEvent并清空用户权限信息
    @EventListener
    public void dispatchDimensionUnbind(EntityDeletedEvent<DimensionUserEntity> event) {
        event.async(
                this.publishEvent(Flux.fromIterable(event.getEntity()), DimensionUnbindEvent::new)
                    .then(
                            this.clearUserCache(event.getEntity())
                    )
        );
    }

    //修改绑定信息时清空权限
    @EventListener
    public void handleModifyEvent(EntityModifyEvent<DimensionUserEntity> event) {
        event.async(
                this.clearUserCache(event.getAfter())
        );
    }

    //维度被删除时同时删除绑定信息
    @EventListener
    public void handleDimensionDeletedEntity(EntityDeletedEvent<DimensionEntity> event) {
        event.async(
                Flux.fromIterable(event.getEntity())
                    .collect(groupingBy(DimensionEntity::getTypeId,
                                        mapping(DimensionEntity::getId, toSet())))
                    .flatMapIterable(Map::entrySet)
                    .flatMap(entry -> this
                            .createDelete()
                            .where(DimensionUserEntity::getDimensionTypeId, entry.getKey())
                            .in(DimensionUserEntity::getDimensionId, entry.getValue())
                            .execute())
        );

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

    private Mono<Void> clearUserCache(List<DimensionUserEntity> entities) {
        return Flux.fromIterable(entities)
                   .map(DimensionUserEntity::getUserId)
                   .distinct()
                   .collectList()
                   .map(ClearUserAuthorizationCacheEvent::of)
                   .doOnNext(eventPublisher::publishEvent)
                   .then();
    }

}