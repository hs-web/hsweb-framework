package org.hsweb.web.service.impl;

import org.hsweb.ezorm.core.ObjectWrapperFactory;
import org.hsweb.ezorm.core.ValidatorFactory;
import org.hsweb.ezorm.rdb.RDBDatabase;
import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hsweb.ezorm.rdb.meta.RDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.meta.parser.H2TableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.MysqlTableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.OracleTableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.TableMetaParser;
import org.hsweb.ezorm.rdb.render.dialect.H2RDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.dialect.MysqlRDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.dialect.OracleRDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.simple.SimpleDatabase;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.hsweb.web.core.datasource.DataSourceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@AutoConfigureOrder
public class DataBaseAutoConfiguration {
    @Resource
    private SqlExecutor sqlExecutor;

    @Autowired(required = false)
    private ValidatorFactory validatorFactory;

    @Autowired(required = false)
    private ObjectWrapperFactory objectWrapperFactory;

    @Autowired(required = false)
    private Map<String, ExpressionScopeBean> expressionScopeBeanMap;

    @Bean
    @ConditionalOnMissingBean(TableMetaParser.class)
    public TableMetaParser tableMetaParser() {
        return DataSourceHolder.getActiveDatabaseType().getDialect().getDefaultParser(sqlExecutor);
    }

    @Bean
    public RDBDatabase database(javax.sql.DataSource dataSource) throws SQLException {
        DataSourceHolder holder = new DataSourceHolder();
        holder.init(dataSource);
        RDBDatabaseMetaData dataBaseMetaData;
        switch (DataSourceHolder.getDefaultDatabaseType()) {
            case mysql:
                dataBaseMetaData = new MysqlRDBDatabaseMetaData();
                break;
            case oracle:
                dataBaseMetaData = new OracleRDBDatabaseMetaData();
                break;
            case h2:
                dataBaseMetaData = new H2RDBDatabaseMetaData();
                break;
            default:
                dataBaseMetaData = new H2RDBDatabaseMetaData();
                break;
        }
        if (objectWrapperFactory != null)
            dataBaseMetaData.setObjectWrapperFactory(objectWrapperFactory);
        if (validatorFactory != null)
            dataBaseMetaData.setValidatorFactory(validatorFactory);
        dataBaseMetaData.init();
        SimpleDatabase dataBase = new SimpleDatabase(dataBaseMetaData, sqlExecutor) {
            @Override
            public Map<String, Object> getTriggerContextRoot() {
                if (expressionScopeBeanMap != null)
                    return new HashMap<>(expressionScopeBeanMap);
                return super.getTriggerContextRoot();
            }
        };
        return dataBase;
    }
}
