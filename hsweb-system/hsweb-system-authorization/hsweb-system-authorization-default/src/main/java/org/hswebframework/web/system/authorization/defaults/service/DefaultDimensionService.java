package org.hswebframework.web.system.authorization.defaults.service;

import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.authorization.DimensionProvider;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.crud.service.ReactiveTreeSortEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.system.authorization.api.entity.AuthorizationSettingEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionTypeEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;
import org.hswebframework.web.system.authorization.api.event.ClearUserAuthorizationCacheEvent;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultDimensionService
        extends GenericReactiveCrudService<DimensionEntity, String>
        implements ReactiveTreeSortEntityService<DimensionEntity, String>,
        DimensionProvider {

    @Autowired
    private ReactiveRepository<DimensionUserEntity, String> dimensionUserRepository;

    @Autowired
    private ReactiveRepository<DimensionTypeEntity, String> dimensionTypeRepository;

    @Autowired
    private ReactiveRepository<AuthorizationSettingEntity, String> settingRepository;

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

    @Override
    public Flux<DimensionTypeEntity> getAllType() {
        return dimensionTypeRepository
                .createQuery()
                .fetch();
    }

    @Override
    public Flux<DynamicDimension> getDimensionByUserId(String userId) {
        return getAllType()
                .collect(Collectors.toMap(DimensionType::getId, Function.identity()))
                .flatMapMany(typeGrouping ->
                        dimensionUserRepository
                                .createQuery()
                                .where(DimensionUserEntity::getUserId, userId)
                                .fetch()
                                .collectList()
                                .filter(CollectionUtils::isNotEmpty)
                                .flatMapMany(list -> {
                                    //查询所有的维度
                                    return this.queryIncludeChildren(list.stream()
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
    @SuppressWarnings("all")
    public Flux<String> getUserIdByDimensionId(String dimensionId) {
        return dimensionUserRepository
                .createQuery()
                .select(DimensionUserEntity::getUserId)
                .where(DimensionUserEntity::getDimensionId, dimensionId)
                .fetch()
                .map(DimensionUserEntity::getUserId);
    }

    @Override
    public Mono<SaveResult> save(Publisher<DimensionEntity> entityPublisher) {
        return super.save(entityPublisher)
                .doOnSuccess((r) -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.all()));
    }

    @Override
    public Mono<Integer> updateById(String id, Mono<DimensionEntity> entityPublisher) {
        return super.updateById(id, entityPublisher)
                .doOnSuccess((r) -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.all()));
    }

    @Override
    public ReactiveUpdate<DimensionEntity> createUpdate() {
        return super.createUpdate()
                .onExecute((update, result) -> result.doOnSuccess((r) -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.all())));
    }

    @Override
    public ReactiveDelete createDelete() {
        return super.createDelete()
                .onExecute((delete, result) -> result.doOnSuccess((r) -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.all())));
    }

    @Override
    public Mono<Integer> deleteById(Publisher<String> idPublisher) {
        return Flux.from(idPublisher)
                .collectList()
                .flatMap(list -> super.deleteById(Flux.fromIterable(list))
                        .then(dimensionUserRepository.createDelete() //删除维度用户关联
                                .where()
                                .in(DimensionUserEntity::getDimensionId, list)
                                .execute())
                        .then(findById(Flux.fromIterable(list))
                                .groupBy(DimensionEntity::getTypeId, DimensionEntity::getId)//按维度类型分组
                                .flatMap(grouping -> grouping.collectList()
                                        .flatMap(dimensionId -> settingRepository //删除权限设置
                                                .createDelete()
                                                .where(AuthorizationSettingEntity::getDimensionType, grouping.key())
                                                .in(AuthorizationSettingEntity::getDimensionTarget, dimensionId).execute()))
                                .collect(Collectors.summarizingInt(Integer::intValue))
                        )
                        .doOnSuccess((r) -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.all()))
                        .thenReturn(list.size()));
    }

}
