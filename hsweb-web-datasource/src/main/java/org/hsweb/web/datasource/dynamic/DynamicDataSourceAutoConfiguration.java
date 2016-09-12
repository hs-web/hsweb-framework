/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.datasource.dynamic;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.hsweb.commons.StringUtils;
import org.hsweb.web.core.datasource.DynamicDataSource;
import org.hsweb.web.service.datasource.DynamicDataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import javax.transaction.SystemException;

@Configuration
@ConditionalOnMissingBean(DynamicDataSource.class)
@ComponentScan("org.hsweb.web.datasource.dynamic")
public class DynamicDataSourceAutoConfiguration {

    @Autowired
    private DataSourceProperties properties;

    static {
        //  com.atomikos.icatch.config.Configuration.init();
        //  com.atomikos.icatch.config.Configuration.installCompositeTransactionManager(new CompositeTransactionManagerImp());
    }

    /**
     * 默认数据库链接
     */
    @Primary
    @Bean(initMethod = "init", name = "dataSource", destroyMethod = "close")
    public DataSource dataSource() {
        AtomikosDataSourceBean dataSourceBean = new AtomikosDataSourceBean();
        dataSourceBean.getXaProperties().putAll(properties.getXa().getProperties());
        dataSourceBean.setXaDataSourceClassName(properties.getXa().getDataSourceClassName());
        dataSourceBean.setUniqueResourceName("core");
        dataSourceBean.setMinPoolSize(StringUtils.toInt(properties.getXa().getProperties().get("minPoolSize"), 5));
        dataSourceBean.setMaxPoolSize(StringUtils.toInt(properties.getXa().getProperties().get("maxPoolSize"), 200));
        dataSourceBean.setTestQuery(properties.getXa().getProperties().get("validationQuery"));
        dataSourceBean.setBorrowConnectionTimeout(60);
        return dataSourceBean;
    }

    @Bean(name = "dynamicDataSource")
    public DynamicXaDataSourceImpl dynamicXaDataSource(@Qualifier("dataSource") DataSource dataSource) {
        return new DynamicXaDataSourceImpl(dataSource);
    }

    /**
     * 动态数据源
     */
    @Bean(initMethod = "init", destroyMethod = "close")
    public AtomikosDataSourceBean atomikosDataSourceBean(DynamicXaDataSourceImpl dynamicDataSource) {
        AtomikosDataSourceBean dataSourceBean = new AtomikosDataSourceBean();
        dataSourceBean.setXaDataSource(dynamicDataSource);
        dataSourceBean.setUniqueResourceName("dynamic");
        dataSourceBean.setMaxPoolSize(StringUtils.toInt(properties.getXa().getProperties().get("maxPoolSize"), 200));
        dataSourceBean.setBorrowConnectionTimeout(30);
        return dataSourceBean;
    }

    @Bean
    public UserTransactionManager userTransactionManager() {
        UserTransactionManager transactionManager = new UserTransactionManager();
        transactionManager.setForceShutdown(true);
        return transactionManager;
    }

    @Bean
    public UserTransactionImp userTransaction() throws SystemException {
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        userTransactionImp.setTransactionTimeout(300);
        return userTransactionImp;
    }

    @Bean
    public JtaTransactionManager transactionManager(UserTransactionManager userTransactionManager, UserTransactionImp userTransaction) throws SystemException {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(userTransactionManager);
        jtaTransactionManager.setUserTransaction(userTransaction);
        jtaTransactionManager.setAllowCustomIsolationLevels(true);
        return jtaTransactionManager;
    }

    @Bean(name = "sqlExecutor")
    @ConditionalOnMissingBean(DynamicDataSourceSqlExecutorService.class)
    public DynamicDataSourceSqlExecutorService sqlExecutor() {
        return new DynamicDataSourceSqlExecutorService();
    }

}
