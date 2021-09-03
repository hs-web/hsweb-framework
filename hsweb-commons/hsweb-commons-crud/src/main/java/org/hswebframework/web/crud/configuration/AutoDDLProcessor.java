package org.hswebframework.web.crud.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.hswebframework.web.crud.events.EntityBeforeQueryEvent;
import org.hswebframework.web.crud.events.EntityDDLEvent;
import org.hswebframework.web.event.GenericsPayloadApplicationEvent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private boolean reactive;

    @Override
    @SneakyThrows
    public void afterPropertiesSet() {
        if (entityFactory instanceof MapperEntityFactory) {
            MapperEntityFactory factory = ((MapperEntityFactory) entityFactory);
            for (EntityInfo entity : entities) {
                factory.addMapping(entity.getEntityType(), MapperEntityFactory.defaultMapper(entity.getRealType()));
            }
        }
        List<Class> entities = this.entities.stream().map(EntityInfo::getRealType).collect(Collectors.toList());
        if (properties.isAutoDdl()) {
            //加载全部表信息
            if (reactive) {
                Flux.fromIterable(entities)
                    .doOnNext(type -> log.trace("auto ddl for {}", type))
                    .map(type -> {
                        RDBTableMetadata metadata = resolver.resolve(type);
                        EntityDDLEvent event = new EntityDDLEvent(type,metadata);
                        eventPublisher.publishEvent(new GenericsPayloadApplicationEvent<>(this, event, type));
                        return metadata;
                    })
                    .flatMap(meta -> operator
                            .ddl()
                            .createOrAlter(meta)
                            .autoLoad(false)
                            .commit()
                            .reactive()
                            .subscribeOn(Schedulers.elastic())
                    )
                    .doOnError((err) -> log.error(err.getMessage(), err))
                    .then()
                    .block(Duration.ofMinutes(5));
            } else {
                for (Class<?> type : entities) {
                    log.trace("auto ddl for {}", type);
                    try {
                        RDBTableMetadata metadata = resolver.resolve(type);
                        EntityDDLEvent event = new EntityDDLEvent(type,metadata);
                        eventPublisher.publishEvent(new GenericsPayloadApplicationEvent<>(this, event, type));
                        operator.ddl()
                                .createOrAlter(metadata)
                                .autoLoad(false)
                                .commit()
                                .sync();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        throw e;
                    }
                }
            }
        } else {
            for (Class<?> entity : entities) {
                RDBTableMetadata metadata = resolver.resolve(entity);
                operator.getMetadata()
                        .getCurrentSchema()
                        .addTable(metadata);
            }
        }
    }
}
