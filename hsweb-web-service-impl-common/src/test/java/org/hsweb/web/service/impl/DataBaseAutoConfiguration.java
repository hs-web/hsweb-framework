package org.hsweb.web.service.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.webbuilder.sql.DataBase;
import org.webbuilder.sql.DataBaseMetaData;
import org.webbuilder.sql.support.OracleDataBaseMetaData;
import org.webbuilder.sql.support.common.CommonDataBase;
import org.webbuilder.sql.support.executor.SqlExecutor;

import javax.annotation.Resource;
import java.sql.DatabaseMetaData;

/**
 * Created by zhouhao on 16-4-20.
 */
@Configuration
public class DataBaseAutoConfiguration {
    @Resource
    private SqlExecutor sqlExecutor;

    @Bean
    public DataBase getDataBase() {
        DataBaseMetaData dataBaseMetaData = new OracleDataBaseMetaData();
        DataBase dataBase = new CommonDataBase(dataBaseMetaData, sqlExecutor);
        return dataBase;
    }
}
