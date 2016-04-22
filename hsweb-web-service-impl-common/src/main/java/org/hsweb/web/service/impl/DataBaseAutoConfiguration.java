package org.hsweb.web.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.webbuilder.sql.DataBase;
import org.webbuilder.sql.DataBaseMetaData;
import org.webbuilder.sql.support.MysqlDataBaseMetaData;
import org.webbuilder.sql.support.OracleDataBaseMetaData;
import org.webbuilder.sql.support.common.CommonDataBase;
import org.webbuilder.sql.support.executor.SqlExecutor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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

    @PostConstruct
    public void init() {

    }

    @Bean
    public DataBase getDataBase() {
        DataBaseMetaData dataBaseMetaData = null;
        String driverClassName = properties.getDriverClassName();
        if (driverClassName.contains("mysql")) {
            dataBaseMetaData = new MysqlDataBaseMetaData();
        } else if (driverClassName.contains("oracle")) {
            dataBaseMetaData = new OracleDataBaseMetaData();
        } else if (driverClassName.contains("h2")) {
            dataBaseMetaData = new OracleDataBaseMetaData();
        }

        if (dataBaseMetaData == null)
            dataBaseMetaData = new OracleDataBaseMetaData();
        DataBase dataBase = new CommonDataBase(dataBaseMetaData, sqlExecutor);
        return dataBase;
    }
}
