package org.hswebframework.web.system.authorization.defaults.service;

import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.crud.service.ReactiveTreeSortEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.system.authorization.api.entity.DimensionEntity;
import org.springframework.util.StringUtils;

import java.util.List;

public class DefaultDimensionService
        extends GenericReactiveCrudService<DimensionEntity, String>
        implements ReactiveTreeSortEntityService<DimensionEntity, String> {

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



}
