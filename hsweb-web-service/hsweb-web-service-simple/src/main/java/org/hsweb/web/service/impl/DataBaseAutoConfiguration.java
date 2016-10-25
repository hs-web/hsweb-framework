package org.hsweb.web.service.impl;

import org.hsweb.ezorm.executor.SqlExecutor;
import org.hsweb.ezorm.meta.DatabaseMetaData;
import org.hsweb.ezorm.meta.expand.ObjectWrapperFactory;
import org.hsweb.ezorm.meta.expand.ValidatorFactory;
import org.hsweb.ezorm.meta.parser.H2TableMetaParser;
import org.hsweb.ezorm.meta.parser.MysqlTableMetaParser;
import org.hsweb.ezorm.meta.parser.OracleTableMetaParser;
import org.hsweb.ezorm.meta.parser.TableMetaParser;
import org.hsweb.ezorm.render.dialect.H2DatabaseMeta;
import org.hsweb.ezorm.render.dialect.MysqlDatabaseMeta;
import org.hsweb.ezorm.render.dialect.OracleDatabaseMeta;
import org.hsweb.ezorm.run.Database;
import org.hsweb.ezorm.run.simple.SimpleDatabase;
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
        switch (DataSourceHolder.getDefaultDatabaseType()) {
            case mysql:
                return new MysqlTableMetaParser(sqlExecutor);
            case oracle:
                return new OracleTableMetaParser(sqlExecutor);
            case h2:
                return new H2TableMetaParser(sqlExecutor);
            default:
                return null;
        }
    }

    @Bean
    public Database database(javax.sql.DataSource dataSource) throws SQLException {
        DataSourceHolder holder = new DataSourceHolder();
        holder.init(dataSource);
        DatabaseMetaData dataBaseMetaData;
        switch (DataSourceHolder.getDefaultDatabaseType()) {
            case mysql:
                dataBaseMetaData = new MysqlDatabaseMeta();
                break;
            case oracle:
                dataBaseMetaData = new OracleDatabaseMeta();
                break;
            case h2:
                dataBaseMetaData = new H2DatabaseMeta();
                break;
            default:
                dataBaseMetaData = new OracleDatabaseMeta();
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
