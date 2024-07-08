package org.hswebframework.web.crud.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.ddl.CreateTableSqlBuilder;
import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.crud.annotation.DDL;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.hswebframework.web.crud.events.EntityDDLEvent;
import org.hswebframework.web.event.GenericsPayloadApplicationEvent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.AnnotatedElementUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        List<Class<?>> readyToDDL = new ArrayList<>(this.entities.size());
        List<Class<?>> nonDDL = new ArrayList<>();

        for (EntityInfo entity : this.entities) {
            Class<?> type = entityFactory.getInstanceType(entity.getRealType(), true);
            DDL ddl = AnnotatedElementUtils.findMergedAnnotation(type, DDL.class);
            if (properties.isAutoDdl() && (ddl == null || ddl.value())) {
                readyToDDL.add(entity.getEntityType());
            } else {
                nonDDL.add(entity.getEntityType());
            }
        }

        if (!readyToDDL.isEmpty()) {
            //加载全部表信息
            if (reactive) {
                Flux.fromIterable(readyToDDL)
                    .doOnNext(type -> log.trace("auto ddl for {}", type))
                    .map(type -> {
                        RDBTableMetadata metadata = resolver.resolve(type);
                        EntityDDLEvent<?> event = new EntityDDLEvent<>(this, type, metadata);
                        eventPublisher.publishEvent(new GenericsPayloadApplicationEvent<>(this, event, type));
                        return metadata;
                    })
                    .flatMap(meta -> operator
                                 .ddl()
                                 .createOrAlter(meta)
                                 .autoLoad(false)
                                 .commit()
                                 .reactive()
                                 .subscribeOn(Schedulers.boundedElastic()),
                             8)
                    .doOnError((err) -> log.error(err.getMessage(), err))
                    .then()
                    .block(Duration.ofMinutes(5));
            } else {
                for (Class<?> type : readyToDDL) {
                    log.trace("auto ddl for {}", type);
                    try {
                        RDBTableMetadata metadata = resolver.resolve(type);
                        EntityDDLEvent<?> event = new EntityDDLEvent<>(this, type, metadata);
                        eventPublisher.publishEvent(new GenericsPayloadApplicationEvent<>(this, event, type));
                        operator.ddl()
                                .createOrAlter(metadata)
                                .autoLoad(false)
                                .commit()
                                .sync();
                    } catch (Exception e) {
                        log.error(e.getLocalizedMessage(), e);
                        throw e;
                    }
                }
            }
        }

        for (Class<?> entity : nonDDL) {
            RDBTableMetadata metadata = resolver.resolve(entity);
            RDBSchemaMetadata schema = metadata.getSchema();
            RDBTableMetadata table = schema
                .getTable(metadata.getName())
                .orElse(null);
            if (table == null) {
                SqlRequest request = schema.findFeatureNow(CreateTableSqlBuilder.ID).build(metadata);
                log.info("DDL SQL for {} \n{}", entity, request.toNativeSql());
            }
            schema.addTable(metadata);
        }
    }
}
