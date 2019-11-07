package org.hswebframework.web.system.authorization.defaults.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.DimensionProvider;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.crud.service.ReactiveTreeSortEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.system.authorization.api.entity.AuthorizationSettingEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionTypeEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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

    @Override
    public IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public void setChildren(DimensionEntity entity, List<DimensionEntity> children) {
        entity.setChildren(children);
    }

    @Override
    public Flux<DimensionType> getAllType() {
        return dimensionTypeRepository
                .createQuery()
                .fetch()
                .cast(DimensionType.class);
    }

    @Override
    public Flux<Dimension> getDimensionByUserId(String userId) {
        return getAllType()
                .collect(Collectors.toMap(DimensionType::getId, Function.identity()))
                .flatMapMany(typeGrouping ->
                        dimensionUserRepository
                                .createQuery()
                                .where(DimensionUserEntity::getUserId, userId)
                                .fetch()
                                .map(entity -> DynamicDimension.of(entity, typeGrouping.get(entity.getDimensionTypeId()))));
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
                        ).thenReturn(list.size()));
    }

}
