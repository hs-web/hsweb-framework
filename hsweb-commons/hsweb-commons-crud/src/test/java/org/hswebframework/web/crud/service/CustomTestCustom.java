package org.hswebframework.web.crud.service;

import org.hswebframework.web.crud.entity.CustomTestEntity;
import org.hswebframework.web.crud.entity.TestEntity;
import org.hswebframework.web.crud.entity.factory.EntityMappingCustomizer;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomTestCustom implements EntityMappingCustomizer {
    @Override
    public void custom(MapperEntityFactory factory) {
        factory.addMapping(TestEntity.class, new MapperEntityFactory.Mapper<>(CustomTestEntity.class,CustomTestEntity::new));
    }
}
