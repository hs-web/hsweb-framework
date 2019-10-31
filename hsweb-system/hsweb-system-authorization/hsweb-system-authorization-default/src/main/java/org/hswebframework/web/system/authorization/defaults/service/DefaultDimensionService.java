package org.hswebframework.web.system.authorization.defaults.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.DimensionProvider;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.crud.service.ReactiveTreeSortEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.system.authorization.api.entity.DimensionEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionTypeEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

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

    @Override
    public IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public void setChildren(DimensionEntity entity, List<DimensionEntity> children) {
        entity.setChildren(children);
    }

    @Override
    public List<DimensionEntity> getChildren(DimensionEntity entity) {
        return entity.getChildren();
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
}
