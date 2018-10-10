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

package org.hswebframework.web.starter;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.ezorm.rdb.meta.RDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.meta.parser.H2TableMetaParser;
import org.hswebframework.ezorm.rdb.meta.parser.MysqlTableMetaParser;
import org.hswebframework.ezorm.rdb.meta.parser.OracleTableMetaParser;
import org.hswebframework.ezorm.rdb.render.dialect.H2RDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.render.dialect.MysqlRDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.render.dialect.OracleRDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.simple.SimpleDatabase;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.web.ScriptScope;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.DatabaseType;
import org.hswebframework.web.service.Service;
import org.hswebframework.web.starter.init.SystemInitialize;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhouhao
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class SystemInitializeAutoConfiguration implements CommandLineRunner, BeanPostProcessor {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SqlExecutor sqlExecutor;

    @Autowired
    private ApplicationContext applicationContext;

    private List<DynamicScriptEngine> engines;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        engines = Stream.of("js", "groovy")
                .map(DynamicScriptEngineFactory::getEngine)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        addGlobalVariable("logger", LoggerFactory.getLogger("org.hswebframework.script"));
        addGlobalVariable("sqlExecutor", sqlExecutor);
        addGlobalVariable("spring", applicationContext);
    }

    @SuppressWarnings("all")
    protected void addGlobalVariable(String var, Object val) {
        engines.forEach(engine -> {
                    try {
                        engine.addGlobalVariable(Collections.singletonMap(var, val));
                    } catch (NullPointerException ignore) {
                    }
                }
        );
    }

    @Override
    public void run(String... args) throws Exception {

        if (!appProperties.isAutoInit()) {
            log.debug("app auto init is disabled");
            return;
        }
        DatabaseType type = DataSourceHolder.currentDatabaseType();
        SystemVersion version = appProperties.build();
        if (version.getName() == null) {
            version.setName("unknown");
        }
        Connection connection = null;
        String jdbcUserName;
        try {
            connection = DataSourceHolder.currentDataSource().getNative().getConnection();
            jdbcUserName = connection.getMetaData().getUserName();
        } finally {
            if (null != connection) {
                connection.close();
            }
        }
        RDBDatabaseMetaData metaData;
        switch (type) {
            case oracle:
                metaData = new OracleRDBDatabaseMetaData();
                metaData.setParser(new OracleTableMetaParser(sqlExecutor));
                break;
            case mysql:
                String engine = environment.getProperty("mysql.engine");
                if (StringUtils.hasText(engine)) {
                    metaData = new MysqlRDBDatabaseMetaData(engine);
                } else {
                    metaData = new MysqlRDBDatabaseMetaData();
                }
                metaData.setParser(new MysqlTableMetaParser(sqlExecutor));
                break;
            default:
                metaData = new H2RDBDatabaseMetaData();
                metaData.setParser(new H2TableMetaParser(sqlExecutor));
                break;
        }
        metaData.init();

        SimpleDatabase database = new SimpleDatabase(metaData, sqlExecutor);
        database.setAutoParse(true);
        SystemInitialize initialize = new SystemInitialize(sqlExecutor, database, version);

        initialize.addScriptContext("db", jdbcUserName);
        initialize.addScriptContext("dbType", type.name());

        initialize.install();
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        ScriptScope scope;
        if (bean instanceof Service) {
            addGlobalVariable(beanName, bean);
        } else if ((scope = AnnotationUtils.findAnnotation(ClassUtils.getUserClass(bean), ScriptScope.class)) != null) {
            addGlobalVariable(!scope.value().isEmpty() ? scope.value() : beanName, bean);
        }
        return bean;
    }
}
