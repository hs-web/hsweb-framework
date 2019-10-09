package org.hswebframework.web.crud.configuration;


import io.r2dbc.spi.ConnectionFactory;
import lombok.SneakyThrows;
import org.hswebframework.ezorm.core.meta.Feature;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.mapping.EntityColumnMapping;
import org.hswebframework.ezorm.rdb.mapping.EntityManager;
import org.hswebframework.ezorm.rdb.mapping.MappingFeatureType;
import org.hswebframework.ezorm.rdb.mapping.jpa.JpaEntityTableMetadataParser;
import org.hswebframework.ezorm.rdb.mapping.parser.EntityTableMetadataParser;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.DefaultDatabaseOperator;
import org.hswebframework.web.crud.sql.DefaultJdbcExecutor;
import org.hswebframework.web.crud.annotation.EnableEasyormRepository;
import org.hswebframework.web.crud.entity.factory.EntityFactory;
import org.hswebframework.web.crud.sql.DefaultJdbcReactiveExecutor;
import org.hswebframework.web.crud.sql.DefaultR2dbcExecutor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableConfigurationProperties(EasyormProperties.class)
@EnableEasyormRepository("org.hswebframework.web.**.entity")
public class EasyOrmConfiguration {

    @Autowired
    private EasyormProperties properties;

    @Configuration
    @ConditionalOnBean(DataSource.class)
    public static class JdbcSqlExecutorConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public SyncSqlExecutor syncSqlExecutor() {
            return new DefaultJdbcExecutor();
        }

        @Bean
        @ConditionalOnMissingBean
        public ReactiveSqlExecutor reactiveSqlExecutor() {
            return new DefaultJdbcReactiveExecutor();
        }

    }

    @Configuration
    @ConditionalOnClass(ConnectionFactory.class)
    public static class R2dbcSqlExecutorConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public ReactiveSqlExecutor reactiveSqlExecutor() {
            return new DefaultR2dbcExecutor();
        }

        @Bean
        @ConditionalOnMissingBean
        public SyncSqlExecutor syncSqlExecutor(ReactiveSqlExecutor reactiveSqlExecutor) {
            return ReactiveSyncSqlExecutor.of(reactiveSqlExecutor);
        }
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
    public EntityTableMetadataParser jpaEntityTableMetadataParser(DatabaseOperator operator) {
        JpaEntityTableMetadataParser parser = new JpaEntityTableMetadataParser();
        parser.setDatabaseMetadata(operator.getMetadata());

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
