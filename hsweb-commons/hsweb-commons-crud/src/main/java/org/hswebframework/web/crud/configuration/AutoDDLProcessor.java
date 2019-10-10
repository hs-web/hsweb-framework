package org.hswebframework.web.crud.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
public class AutoDDLProcessor {

    private Set<EntityInfo> entities = new HashSet<>();

    @Autowired
    private DatabaseOperator operator;

    @Autowired
    private EasyormProperties properties;

    @Autowired
    private EntityTableMetadataResolver resolver;

    @Autowired
    private EntityFactory entityFactory;

    private boolean reactive;

    public void init() {
        if(entityFactory instanceof MapperEntityFactory){
            MapperEntityFactory factory= ((MapperEntityFactory) entityFactory);

            for (EntityInfo entity : entities) {
                factory.addMapping(entity.getEntityType(),MapperEntityFactory.defaultMapper(entity.getRealType()));
            }
        }

        if (properties.isAutoDdl()) {
            List<Class> entities = this.entities.stream().map(EntityInfo::getRealType).collect(Collectors.toList());
            if(reactive){
                Flux.fromIterable(entities)
                        .doOnNext(type -> log.info("auto ddl for {}", type))
                        .map(resolver::resolve)
                        .flatMap(meta -> operator.ddl().createOrAlter(meta).commit().reactive())
                        .onErrorContinue((err, a) -> log.warn(err.getMessage(), err))
                        .then()
                        .block();
            }else{
                for (Class entity : entities) {
                    log.warn("auto ddl for {}", entity);
                    try {
                        operator.ddl()
                                .createOrAlter(resolver.resolve(entity))
                                .commit()
                                .sync();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    }
}
