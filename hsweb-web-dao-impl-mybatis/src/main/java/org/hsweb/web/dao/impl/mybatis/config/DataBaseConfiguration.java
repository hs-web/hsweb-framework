package org.hsweb.web.dao.impl.mybatis.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Created by 浩 on 2016-01-22 0022.
 */
@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "org.hsweb.web.dao", sqlSessionFactoryRef = "sqlSessionFactory")
public class DataBaseConfiguration implements EnvironmentAware {
    private RelaxedPropertyResolver propertyResolver;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void setEnvironment(Environment env) {
        this.propertyResolver = new RelaxedPropertyResolver(env, "jdbc.");
    }

    @Bean(name = "dataSource", destroyMethod = "close", initMethod = "init")
    public DataSource writeDataSource() {
        log.debug("Configruing  DataSource");
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(propertyResolver.getProperty("url"));
        datasource.setDriverClassName(propertyResolver.getProperty("driverClassName"));
        datasource.setUsername(propertyResolver.getProperty("username"));
        datasource.setPassword(propertyResolver.getProperty("password"));
        return datasource;
    }
}
