/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.dao.mybatis;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.dao.mybatis.builder.EasyOrmSqlBuilder;
import org.hswebframework.web.dao.mybatis.dynamic.DynamicDataSourceSqlSessionFactoryBuilder;
import org.hswebframework.web.dao.mybatis.dynamic.DynamicSpringManagedTransaction;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(MybatisProperties.class)
public class MyBatisAutoConfiguration {

    @Autowired(required = false)
    private Interceptor[] interceptors;

    @Autowired
    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Autowired(required = false)
    private DatabaseIdProvider databaseIdProvider;

    @Autowired(required = false)
    private EntityFactory entityFactory;

    @Bean
    @Primary
    @ConfigurationProperties(prefix = MybatisProperties.MYBATIS_PREFIX)
    public MybatisProperties mybatisProperties() {
        return new MybatisProperties();
    }

    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        MybatisProperties mybatisProperties = this.mybatisProperties();
        if (null != entityFactory) {
            factory.setObjectFactory(new MybatisEntityFactory(entityFactory));
        }
        factory.setVfs(SpringBootVFS.class);
        if (mybatisProperties().isDynamicDatasource()) {
            factory.setSqlSessionFactoryBuilder(new DynamicDataSourceSqlSessionFactoryBuilder());
            factory.setTransactionFactory(new SpringManagedTransactionFactory() {
                @Override
                public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
                    return new DynamicSpringManagedTransaction();
                }
            });
        }
        factory.setDataSource(dataSource);
        if (StringUtils.hasText(mybatisProperties.getConfigLocation())) {
            factory.setConfigLocation(this.resourceLoader.getResource(mybatisProperties
                    .getConfigLocation()));
        }
        if (mybatisProperties.getConfiguration() != null) {
            factory.setConfiguration(mybatisProperties.getConfiguration());
        }
        if (this.interceptors != null && this.interceptors.length > 0) {
            factory.setPlugins(this.interceptors);
        }
        if (this.databaseIdProvider != null) {
            factory.setDatabaseIdProvider(this.databaseIdProvider);
        }
        factory.setTypeAliasesPackage(mybatisProperties.getTypeAliasesPackage());
        String typeHandlers = "org.hswebframework.web.dao.mybatis.handler";
        if (mybatisProperties.getTypeHandlersPackage() != null) {
            typeHandlers = typeHandlers + ";" + mybatisProperties.getTypeHandlersPackage();
        }
        factory.setTypeHandlersPackage(typeHandlers);
        factory.setMapperLocations(mybatisProperties.resolveMapperLocations());

        SqlSessionFactory sqlSessionFactory = factory.getObject();
        MybatisUtils.sqlSession = sqlSessionFactory;

        EnumDictHandlerRegister.typeHandlerRegistry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
        EnumDictHandlerRegister.register("org.hswebframework.web;" + mybatisProperties.getTypeHandlersPackage());

        try {
            Class.forName("javax.persistence.Table");
            EasyOrmSqlBuilder.getInstance().useJpa = mybatisProperties.isUseJpa();
        } catch (@SuppressWarnings("all") Exception ignore) {
        }
        EasyOrmSqlBuilder.getInstance().entityFactory = entityFactory;

        return sqlSessionFactory;
    }


}
