package org.hswebframework.web.starter.easyorm;


import lombok.SneakyThrows;
import org.hswebframework.ezorm.core.meta.Feature;
import org.hswebframework.ezorm.rdb.mapping.EntityColumnMapping;
import org.hswebframework.ezorm.rdb.mapping.EntityManager;
import org.hswebframework.ezorm.rdb.mapping.MappingFeatureType;
import org.hswebframework.ezorm.rdb.mapping.jpa.JpaEntityTableMetadataParser;
import org.hswebframework.ezorm.rdb.mapping.parser.EntityTableMetadataParser;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.DefaultDatabaseOperator;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
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
@EnableEasyormRepository("org.hswebframework.web.**.*Entity")
public class EasyOrmConfiguration {

    @Autowired
    private EasyormProperties properties;

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
    public EntityTableMetadataParser jpaEntityTableMetadataParser(DatabaseOperator operator) {
        JpaEntityTableMetadataParser parser = new JpaEntityTableMetadataParser();
        parser.setDatabaseMetadata(operator.getMetadata());
//
//        resolver.ifPresent(parser::setDataTypeResolver);
//        codecResolver.ifPresent(parser::setValueCodecResolver);

        return parser;
    }

    @Bean
    @ConditionalOnMissingBean
    public DatabaseOperator databaseOperator() {
        RDBDatabaseMetadata metadata = properties.createDatabaseMetadata();

        return DefaultDatabaseOperator.of(metadata);
    }

    @Bean
    public BeanPostProcessor autoRegisterFeature(DatabaseOperator operator) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof Feature) {
                    operator.getMetadata().addFeature(((Feature) bean));
                }
                return bean;
            }
        };
    }

}
