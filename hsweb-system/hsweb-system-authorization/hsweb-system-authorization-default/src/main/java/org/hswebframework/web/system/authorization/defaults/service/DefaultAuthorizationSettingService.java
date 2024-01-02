package org.hswebframework.web.system.authorization.defaults.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.authorization.DimensionProvider;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.crud.events.EntityCreatedEvent;
import org.hswebframework.web.crud.events.EntityDeletedEvent;
import org.hswebframework.web.crud.events.EntityModifyEvent;
import org.hswebframework.web.crud.events.EntitySavedEvent;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.system.authorization.api.entity.AuthorizationSettingEntity;
import org.hswebframework.web.system.authorization.api.event.ClearUserAuthorizationCacheEvent;
import org.hswebframework.web.system.authorization.api.event.DimensionDeletedEvent;
import org.hswebframework.web.system.authorization.defaults.configuration.PermissionProperties;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultAuthorizationSettingService extends GenericReactiveCrudService<AuthorizationSettingEntity, String> {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private List<DimensionProvider> providers;

    protected AuthorizationSettingEntity generateId(AuthorizationSettingEntity entity) {
        if (ObjectUtils.isEmpty(entity.getId())) {
            entity.setId(DigestUtils.md5Hex(entity.getPermission() + entity.getDimensionType() + entity.getDimensionTarget()));
        }
        return entity;
    }

    @Override
    public Mono<SaveResult> save(AuthorizationSettingEntity data) {
        generateId(data);
        return super.save(data);
    }

    @Override
    public Mono<SaveResult> save(Collection<AuthorizationSettingEntity> collection) {
        collection.forEach(this::generateId);
        return super.save(collection);
    }

    @Override
    public Mono<SaveResult> save(Publisher<AuthorizationSettingEntity> entityPublisher) {
        return Flux.from(entityPublisher)
                   .map(this::generateId)
                   .as(super::save);
    }

    @Override
    public Mono<Integer> insert(Publisher<AuthorizationSettingEntity> entityPublisher) {

        return Flux.from(entityPublisher)
                   .map(this::generateId)
                   .as(super::insert);
    }

    @Override
    public Mono<Integer> insertBatch(Publisher<? extends Collection<AuthorizationSettingEntity>> entityPublisher) {
        return Flux
                .from(entityPublisher)
                .doOnNext(list -> list.forEach(this::generateId))
                .as(super::insertBatch);
    }

    protected Mono<Void> clearUserAuthCache(List<AuthorizationSettingEntity> settings) {
        return Flux
                .fromIterable(providers)
                .flatMap(provider ->
                                 //按维度类型进行映射
                                 provider.getAllType()
                                         .map(DimensionType::getId)
                                         .map(t -> Tuples.of(t, provider)))
                .collectMap(Tuple2::getT1, Tuple2::getT2)
                .flatMapMany(typeProviderMapping -> Flux
                        .fromIterable(settings)//根据维度获取所有userId
                        .flatMap(setting -> Mono
                                .justOrEmpty(typeProviderMapping.get(setting.getDimensionType()))
                                .flatMapMany(provider -> provider.getUserIdByDimensionId(setting.getDimensionTarget()))))
                .collectList()
                .flatMap(lst-> ClearUserAuthorizationCacheEvent.of(lst).publish(eventPublisher))
                .then();
    }

    @EventListener
    public void handleAuthSettingDeleted(EntityDeletedEvent<AuthorizationSettingEntity> event) {
        event.async(
                clearUserAuthCache(event.getEntity())
        );
    }

    @EventListener
    public void handleAuthSettingChanged(EntityModifyEvent<AuthorizationSettingEntity> event) {
        event.async(
                clearUserAuthCache(event.getAfter())
        );
    }

    @EventListener
    public void handleAuthSettingSaved(EntitySavedEvent<AuthorizationSettingEntity> event) {
        event.async(
                clearUserAuthCache(event.getEntity())
        );
    }

    @EventListener
    public void handleAuthSettingAdded(EntityCreatedEvent<AuthorizationSettingEntity> event) {
        event.async(
                clearUserAuthCache(event.getEntity())
        );
    }

    @EventListener
    public void handleDimensionAdd(DimensionDeletedEvent event) {
        event.async(
                createDelete()
                        .where(AuthorizationSettingEntity::getDimensionType, event.getDimensionType())
                        .and(AuthorizationSettingEntity::getDimensionTarget, event.getDimensionId())
                        .execute()
        );
    }

    @EventListener
    public void handleDimensionDeletedEvent(DimensionDeletedEvent event) {
        event.async(
                createDelete()
                        .where(AuthorizationSettingEntity::getDimensionType, event.getDimensionType())
                        .and(AuthorizationSettingEntity::getDimensionTarget, event.getDimensionId())
                        .execute()
        );
    }
}
