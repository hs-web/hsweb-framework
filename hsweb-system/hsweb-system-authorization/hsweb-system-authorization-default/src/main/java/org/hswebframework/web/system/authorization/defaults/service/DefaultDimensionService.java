package org.hswebframework.web.system.authorization.defaults.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.DimensionProvider;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.authorization.dimension.DimensionUserBind;
import org.hswebframework.web.authorization.dimension.DimensionUserBindProvider;
import org.hswebframework.web.crud.events.EntityDeletedEvent;
import org.hswebframework.web.crud.events.EntityModifyEvent;
import org.hswebframework.web.crud.events.EntitySavedEvent;
import org.hswebframework.web.crud.service.GenericReactiveTreeSupportCrudService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.system.authorization.api.entity.DimensionEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionTypeEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;
import org.hswebframework.web.system.authorization.api.event.ClearUserAuthorizationCacheEvent;
import org.hswebframework.web.system.authorization.api.event.DimensionDeletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Deprecated
public class DefaultDimensionService
    extends GenericReactiveTreeSupportCrudService<DimensionEntity, String>
    implements
    DimensionProvider, DimensionUserBindProvider {

    @Autowired
    private ReactiveRepository<DimensionUserEntity, String> dimensionUserRepository;

    @Autowired
    private ReactiveRepository<DimensionTypeEntity, String> dimensionTypeRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public void setChildren(DimensionEntity entity, List<DimensionEntity> children) {
        entity.setChildren(children);
    }

    private Flux<DimensionTypeEntity> allCache;

    @Override
    public Flux<DimensionTypeEntity> getAllType() {
        if (allCache == null) {
            return allCache = Flux
                .defer(()-> dimensionTypeRepository.createQuery().fetch())
                .cache(Duration.ofSeconds(1));
        }
        return allCache;
    }

    @Override
    public Mono<DynamicDimension> getDimensionById(DimensionType type, String id) {
        return createQuery()
            .where(DimensionEntity::getId, id)
            .fetch()
            .singleOrEmpty()
            .map(entity -> DynamicDimension.of(entity, type));
    }

    @Override
    public Flux<? extends Dimension> getDimensionsById(DimensionType type, Collection<String> idList) {
        return this.createQuery()
                   .where(DimensionEntity::getTypeId, type.getId())
                   .in(DimensionEntity::getId, idList)
                   .fetch()
                   .map(entity -> DynamicDimension.of(entity, type));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<DynamicDimension> getDimensionByUserId(String userId) {
        return getAllType()
            .collect(Collectors.toMap(DimensionType::getId, Function.identity()))
            .filter(MapUtils::isNotEmpty)
            .flatMapMany(typeGrouping -> dimensionUserRepository
                .createQuery()
                .where(DimensionUserEntity::getUserId, userId)
                .fetch()
                .collectList()
                .filter(CollectionUtils::isNotEmpty)
                .flatMapMany(list -> {
                    //查询所有的维度
                    return this
                        .queryIncludeChildren(list.stream()
                                                  .map(DimensionUserEntity::getDimensionId)
                                                  .collect(Collectors.toSet()))
                        .filter(dimension -> typeGrouping.containsKey(dimension.getTypeId()))
                        .map(dimension ->
                                 DynamicDimension.of(dimension, typeGrouping.get(dimension.getTypeId()))
                        );

                })
            );
    }

    @Override
    public Flux<DimensionUserBind> getDimensionBindInfo(Collection<String> userIdList) {
        return dimensionUserRepository
            .createQuery()
            .in(DimensionUserEntity::getUserId, userIdList)
            .fetch()
            .map(entity -> DimensionUserBind.of(entity.getUserId(), entity.getDimensionTypeId(), entity.getDimensionId()));
    }

    @Override
    @SuppressWarnings("all")
    public Flux<String> getUserIdByDimensionId(String dimensionId) {
        return dimensionUserRepository
            .createQuery()
            .select(DimensionUserEntity::getUserId)
            .where(DimensionUserEntity::getDimensionId, dimensionId)
            .fetch()
            .map(DimensionUserEntity::getUserId);
    }

    @EventListener
    public void handleDimensionChanged(EntitySavedEvent<DimensionEntity> event) {
        event.async(
            ClearUserAuthorizationCacheEvent.all().publish(eventPublisher)
        );
    }

    @EventListener
    public void handleDimensionChanged(EntityModifyEvent<DimensionEntity> event) {
        event.async(
            ClearUserAuthorizationCacheEvent.all().publish(eventPublisher)
        );
    }

    @EventListener
    public void dispatchDimensionDeleteEvent(EntityDeletedEvent<DimensionEntity> event) {

        event.async(
            Flux.fromIterable(event.getEntity())
                .flatMap(e -> new DimensionDeletedEvent(e.getTypeId(), e.getId()).publish(eventPublisher))
        );
    }

}
