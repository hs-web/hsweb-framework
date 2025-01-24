package org.hswebframework.web.crud.configuration;


import lombok.SneakyThrows;
import org.hswebframework.ezorm.core.meta.Feature;
import org.hswebframework.ezorm.rdb.events.EventListener;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSqlExecutor;
import org.hswebframework.ezorm.rdb.mapping.EntityColumnMapping;
import org.hswebframework.ezorm.rdb.mapping.EntityManager;
import org.hswebframework.ezorm.rdb.mapping.MappingFeatureType;
import org.hswebframework.ezorm.rdb.mapping.jpa.JpaEntityTableMetadataParser;
import org.hswebframework.ezorm.rdb.mapping.jpa.JpaEntityTableMetadataParserProcessor;
import org.hswebframework.ezorm.rdb.mapping.parser.EntityTableMetadataParser;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.DefaultDatabaseOperator;
import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.crud.annotation.EnableEasyormRepository;
import org.hswebframework.web.crud.entity.factory.EntityMappingCustomizer;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.hswebframework.web.crud.events.*;
import org.hswebframework.web.crud.events.expr.SpelSqlExpressionInvoker;
import org.hswebframework.web.crud.generator.*;
import org.hswebframework.web.crud.query.DefaultQueryHelper;
import org.hswebframework.web.crud.query.QueryHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@AutoConfiguration
@EnableConfigurationProperties(EasyormProperties.class)
@EnableEasyormRepository("org.hswebframework.web.**.entity")
public class EasyormConfiguration {

    static {

    }

    @Bean
    @ConditionalOnMissingBean
    public EntityFactory entityFactory(ObjectProvider<EntityMappingCustomizer> customizers) {
        MapperEntityFactory factory = new MapperEntityFactory();
        for (EntityMappingCustomizer customizer : customizers) {
            customizer.custom(factory);
        }
        return factory;
    }

    @Bean
    @Primary
    public EventListener easyormEventListener(ObjectProvider<EventListener> eventListeners) {
        CompositeEventListener eventListener = new CompositeEventListener();
        eventListeners.forEach(eventListener::addListener);
        return eventListener;
    }

    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("all")
    public RDBDatabaseMetadata databaseMetadata(Optional<SyncSqlExecutor> syncSqlExecutor,
                                                Optional<ReactiveSqlExecutor> reactiveSqlExecutor,
                                                ObjectProvider<Feature> features,
                                                EasyormProperties properties) {
        RDBDatabaseMetadata metadata = properties.createDatabaseMetadata();
        syncSqlExecutor.ifPresent(metadata::addFeature);
        reactiveSqlExecutor.ifPresent(metadata::addFeature);
        features.forEach(metadata::addFeature);

        if (properties.isAutoDdl() && reactiveSqlExecutor.isPresent()) {
            for (RDBSchemaMetadata schema : metadata.getSchemas()) {
                schema.loadAllTableReactive()
                      .block(Duration.ofSeconds(30));
            }
        }
        return metadata;
    }

    @Bean
    @ConditionalOnMissingBean
    public DatabaseOperator databaseOperator(RDBDatabaseMetadata metadata) {

        return DefaultDatabaseOperator.of(metadata);
    }

    @Bean
    public QueryHelper queryHelper(DatabaseOperator databaseOperator) {
        return new DefaultQueryHelper(databaseOperator);
    }

//    @Bean
//    public BeanPostProcessor autoRegisterFeature(RDBDatabaseMetadata metadata) {
//        CompositeEventListener eventListener = new CompositeEventListener();
//        metadata.addFeature(eventListener);
//        return new BeanPostProcessor() {
//            @Override
//            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//
//                if (bean instanceof EventListener) {
//                    eventListener.addListener(((EventListener) bean));
//                } else if (bean instanceof Feature) {
//                    metadata.addFeature(((Feature) bean));
//                }
//
//                return bean;
//            }
//        };
//    }
//

    @Bean
    public CreatorEventListener creatorEventListener() {
        return new CreatorEventListener();
    }


    @Bean
    public ValidateEventListener validateEventListener() {
        return new ValidateEventListener();
    }

    @Bean
    public EntityEventListener entityEventListener(ApplicationEventPublisher eventPublisher,
                                                   ObjectProvider<SqlExpressionInvoker> invokers,
                                                   ObjectProvider<EntityEventListenerCustomizer> customizers) {
        DefaultEntityEventListenerConfigure configure = new DefaultEntityEventListenerConfigure();
        customizers.forEach(customizer -> customizer.customize(configure));
        EntityEventListener entityEventListener = new EntityEventListener(eventPublisher, configure);
        entityEventListener.setExpressionInvoker(invokers.getIfAvailable(SpelSqlExpressionInvoker::new));

        return entityEventListener;
    }

    @Bean
    @ConfigurationProperties(prefix = "easyorm.default-value-generator")
    public DefaultIdGenerator defaultIdGenerator() {

        return new DefaultIdGenerator();
    }

    @Bean
    public MD5Generator md5Generator() {
        return new MD5Generator();
    }

    @Bean
    public SnowFlakeStringIdGenerator snowFlakeStringIdGenerator() {
        return new SnowFlakeStringIdGenerator();
    }

    @Bean
    public RandomIdGenerator randomIdGenerator() {
        return new RandomIdGenerator();
    }

    @Bean
    public CurrentTimeGenerator currentTimeGenerator() {
        return new CurrentTimeGenerator();
    }

    @Configuration
    public static class EntityTableMetadataParserConfiguration {

        @Bean
        public DefaultEntityResultWrapperFactory defaultEntityResultWrapperFactory(EntityManager entityManager) {
            return new DefaultEntityResultWrapperFactory(entityManager);
        }

        @Bean
        @ConditionalOnMissingBean
        public EntityManager entityManager(EntityTableMetadataResolver resolver, EntityFactory entityFactory) {
            return new EntityManager() {
                @Override
                @SneakyThrows
                public <E> E newInstance(Class<E> type) {
                    return entityFactory.newInstance(type);
                }

                @Override
                public EntityColumnMapping getMapping(Class entity) {

                    return resolver.resolve(entity)
                                   .getFeature(MappingFeatureType.columnPropertyMapping.createFeatureId(entity))
                                   .map(EntityColumnMapping.class::cast)
                                   .orElse(null);
                }
            };
        }

        @Bean
        @ConditionalOnMissingBean
        public EntityTableMetadataResolver entityTableMappingResolver(ObjectProvider<EntityTableMetadataParser> parsers) {
            CompositeEntityTableMetadataResolver resolver = new CompositeEntityTableMetadataResolver();
            parsers.forEach(resolver::addParser);
            return resolver;
        }

        @Bean
        @ConditionalOnMissingBean
        public EntityTableMetadataParser jpaEntityTableMetadataParser(ApplicationContext context,
                                                                      EntityFactory factory,
                                                                      ObjectProvider<TableMetadataCustomizer> customizers) {

            JpaEntityTableMetadataParser parser = new JpaEntityTableMetadataParser() {

                @Override
                public Optional<RDBTableMetadata> parseTableMetadata(Class<?> entityType) {
                    Class<?> realType = factory.getInstanceType(entityType, true);
                    Optional<RDBTableMetadata> tableOpt = super.parseTableMetadata(realType);
                    tableOpt.ifPresent(table -> {
                        EntityColumnMapping columnMapping = table.findFeatureNow(
                            MappingFeatureType.columnPropertyMapping.createFeatureId(realType)
                        );
                        if (realType != entityType) {
                            table.addFeature(new DetectEntityColumnMapping(realType, columnMapping, factory));
                            table.addFeature(columnMapping = new DetectEntityColumnMapping(entityType, columnMapping, factory));
                        }
                        for (TableMetadataCustomizer customizer : customizers) {
                            customizer.customTable(realType, table);
                        }
                        columnMapping.reload();
                    });
                    return tableOpt;
                }

                @Override
                protected JpaEntityTableMetadataParserProcessor createProcessor(RDBTableMetadata table, Class<?> type) {
                    Class<?> realType = factory.getInstanceType(type, true);
                    return new JpaEntityTableMetadataParserProcessor(table, realType) {
                        @Override
                        protected void customColumn(PropertyDescriptor descriptor,
                                                    Field field,
                                                    RDBColumnMetadata column,
                                                    Set<Annotation> annotations) {
                            super.customColumn(descriptor, field, column, annotations);
                            for (TableMetadataCustomizer customizer : customizers) {
                                customizer.customColumn(realType, descriptor, field, annotations, column);
                            }
                        }
                    };
                }
            };
            parser.setDatabaseMetadata(()->context.getBean(RDBDatabaseMetadata.class));

            return parser;
        }
    }
}
