package org.hswebframework.web.crud.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
public class AutoDDLProcessor implements InitializingBean {

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

    @Override
    public void afterPropertiesSet() {
        if (entityFactory instanceof MapperEntityFactory) {
            MapperEntityFactory factory = ((MapperEntityFactory) entityFactory);
            for (EntityInfo entity : entities) {
                factory.addMapping(entity.getEntityType(), MapperEntityFactory.defaultMapper(entity.getRealType()));
            }
        }
        if (properties.isAutoDdl()) {
            //加载全部表信息
            operator.getMetadata()
                    .getCurrentSchema()
                    .loadAllTable();

            List<Class> entities = this.entities.stream().map(EntityInfo::getRealType).collect(Collectors.toList());
            if (reactive) {
                Flux.fromIterable(entities)
                        .doOnNext(type -> log.info("auto ddl for {}", type))
                        .map(resolver::resolve)
                        .flatMap(meta -> operator.ddl()
                                .createOrAlter(meta)
                                .commit()
                                .reactive())
                        .doOnError((err) -> log.error(err.getMessage(), err))
                        .then()
                        .block();
            } else {
                for (Class<?> entity : entities) {
                    log.warn("auto ddl for {}", entity);
                    try {
                        operator.ddl()
                                .createOrAlter(resolver.resolve(entity))
                                .commit()
                                .sync();
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }
    }
}
