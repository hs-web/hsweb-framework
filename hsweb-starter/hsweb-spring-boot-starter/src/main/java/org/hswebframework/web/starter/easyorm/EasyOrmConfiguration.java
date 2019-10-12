package org.hswebframework.web.starter.easyorm;


import lombok.SneakyThrows;
import org.hswebframework.ezorm.core.meta.Feature;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.mapping.EntityColumnMapping;
import org.hswebframework.ezorm.rdb.mapping.EntityManager;
import org.hswebframework.ezorm.rdb.mapping.MappingFeatureType;
import org.hswebframework.ezorm.rdb.mapping.jpa.JpaEntityTableMetadataParser;
import org.hswebframework.ezorm.rdb.mapping.parser.EntityTableMetadataParser;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.DefaultDatabaseOperator;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.datasource.DefaultJdbcExecutor;
import org.hswebframework.web.starter.easyorm.generator.MD5Generator;
import org.hswebframework.web.starter.easyorm.generator.SnowFlakeStringIdGenerator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(EasyormProperties.class)
@EnableEasyormRepository("org.hswebframework.web.**.entity")
public class EasyOrmConfiguration {

    @Autowired
    private EasyormProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public SyncSqlExecutor syncSqlExecutor() {
        return new DefaultJdbcExecutor();
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

                return resolver.resolve(entityFactory.getInstanceType(entity))
                        .getFeature(MappingFeatureType.columnPropertyMapping.createFeatureId(entity))
                        .map(EntityColumnMapping.class::cast)
                        .orElse(null);
            }
        };
    }

    @Bean
    public DefaultEntityResultWrapperFactory defaultEntityResultWrapperFactory(EntityManager entityManager) {
        return new DefaultEntityResultWrapperFactory(entityManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityTableMetadataResolver entityTableMappingResolver(List<EntityTableMetadataParser> parsers) {
        CompositeEntityTableMetadataResolver resolver = new CompositeEntityTableMetadataResolver();
        parsers.forEach(resolver::addParser);
        return resolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityTableMetadataParser jpaEntityTableMetadataParser(RDBDatabaseMetadata metadata) {
        JpaEntityTableMetadataParser parser = new JpaEntityTableMetadataParser();
        parser.setDatabaseMetadata(metadata);
        return parser;
    }

    @Bean
    @ConditionalOnMissingBean
    public RDBDatabaseMetadata databaseMetadata(SyncSqlExecutor syncSqlExecutor) {
        RDBDatabaseMetadata metadata = properties.createDatabaseMetadata();
        metadata.addFeature(syncSqlExecutor);
        return metadata;
    }

    @Bean
    @ConditionalOnMissingBean
    public DatabaseOperator databaseOperator(RDBDatabaseMetadata metadata) {

        return DefaultDatabaseOperator.of(metadata);
    }

    @Bean
    public BeanPostProcessor autoRegisterFeature(RDBDatabaseMetadata metadata) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof Feature) {
                    metadata.addFeature(((Feature) bean));
                }
                return bean;
            }
        };
    }

    @Bean
    public MD5Generator md5Generator() {
        return new MD5Generator();
    }

    @Bean
    public SnowFlakeStringIdGenerator snowFlakeStringIdGenerator() {
        return new SnowFlakeStringIdGenerator();
    }


}
