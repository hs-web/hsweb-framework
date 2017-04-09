package org.hsweb.web.starter;

import org.hsweb.ezorm.rdb.RDBDatabase;
import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hsweb.ezorm.rdb.meta.RDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.meta.parser.H2TableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.MysqlTableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.OracleTableMetaParser;
import org.hsweb.ezorm.rdb.render.dialect.H2RDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.dialect.MysqlRDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.dialect.OracleRDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.simple.SimpleDatabase;
import org.hsweb.web.core.datasource.DataSourceHolder;
import org.hsweb.web.core.datasource.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author zhouhao
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class SystemInitializeAutoConfiguration {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    DataSource dataSource;

    @Autowired
    SqlExecutor sqlExecutor;

    @PostConstruct
    public void systemInitialize() throws Exception {
        DatabaseType type = DataSourceHolder.getDefaultDatabaseType();
        SystemVersion version = appProperties.build();
        Connection connection = null;
        String jdbcUserName;
        try {
            connection = DataSourceHolder.getActiveSource().getConnection();
            jdbcUserName = connection.getMetaData().getUserName();
        } finally {
            if (null != connection) connection.close();
        }
        RDBDatabaseMetaData metaData;
        switch (type) {
            case oracle:
                metaData = new OracleRDBDatabaseMetaData();
                metaData.setParser(new OracleTableMetaParser(sqlExecutor));
                break;
            case mysql:
                metaData = new MysqlRDBDatabaseMetaData();
                metaData.setParser(new MysqlTableMetaParser(sqlExecutor));
                break;
            default:
                h2:
                metaData = new H2RDBDatabaseMetaData();
                metaData.setParser(new H2TableMetaParser(sqlExecutor));
                break;
        }

        SimpleDatabase database = new SimpleDatabase(metaData, sqlExecutor);
        database.setAutoParse(true);
        SystemInitialize initialize = new SystemInitialize(version, sqlExecutor, database, jdbcUserName, type.name());
        initialize.afterPropertiesSet();

    }
}
