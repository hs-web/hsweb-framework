package org.hsweb.web.service.impl;

import org.hsweb.ezorm.executor.SqlExecutor;
import org.hsweb.ezorm.meta.DatabaseMetaData;
import org.hsweb.ezorm.meta.expand.ObjectWrapperFactory;
import org.hsweb.ezorm.meta.expand.ValidatorFactory;
import org.hsweb.ezorm.meta.parser.MysqlTableMetaParser;
import org.hsweb.ezorm.meta.parser.OracleTableMetaParser;
import org.hsweb.ezorm.meta.parser.TableMetaParser;
import org.hsweb.ezorm.render.dialect.H2DatabaseMeta;
import org.hsweb.ezorm.render.dialect.MysqlDatabaseMeta;
import org.hsweb.ezorm.render.dialect.OracleDatabaseMeta;
import org.hsweb.ezorm.run.Database;
import org.hsweb.ezorm.run.simple.SimpleDatabase;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouhao on 16-4-20.
 */
@Configuration
@ConfigurationProperties(
        prefix = "spring.datasource"
)
public class DataBaseAutoConfiguration {
    @Resource
    private SqlExecutor sqlExecutor;

    @Autowired
    private DataSourceProperties properties;

    @Autowired(required = false)
    private ValidatorFactory validatorFactory;

    @Autowired(required = false)
    private ObjectWrapperFactory objectWrapperFactory;

    @Autowired(required = false)
    private Map<String, ExpressionScopeBean> expressionScopeBeanMap;


    @Bean
    @ConditionalOnMissingBean(TableMetaParser.class)
    public TableMetaParser tableMetaParser() {
        String driverClassName = properties.getDriverClassName();
        if (driverClassName.contains("mysql")) {
            return new MysqlTableMetaParser(sqlExecutor);
        } else if (driverClassName.contains("oracle")) {
            return new OracleTableMetaParser(sqlExecutor);
        }
        return null;
    }

    @Bean
    public Database database() {
        DatabaseMetaData dataBaseMetaData = null;
        String driverClassName = properties.getDriverClassName();
        if (driverClassName.contains("mysql")) {
            dataBaseMetaData = new MysqlDatabaseMeta();
        } else if (driverClassName.contains("oracle")) {
            dataBaseMetaData = new OracleDatabaseMeta();
        } else if (driverClassName.contains("h2")) {
            dataBaseMetaData = new H2DatabaseMeta();
        }
        if (dataBaseMetaData == null)
            dataBaseMetaData = new OracleDatabaseMeta();
        if (objectWrapperFactory != null)
            dataBaseMetaData.setObjectWrapperFactory(objectWrapperFactory);
        if (validatorFactory != null)
            dataBaseMetaData.setValidatorFactory(validatorFactory);
        dataBaseMetaData.init();
        SimpleDatabase dataBase = new SimpleDatabase(dataBaseMetaData, sqlExecutor) {
            @Override
            public Map<String, Object> getTriggerContextRoot() {
                if (null != null)
                    return new HashMap<>(expressionScopeBeanMap);
                return super.getTriggerContextRoot();
            }
        };
        return dataBase;
    }
}
