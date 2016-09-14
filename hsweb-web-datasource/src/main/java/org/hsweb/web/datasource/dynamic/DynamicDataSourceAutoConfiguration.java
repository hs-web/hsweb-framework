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
import org.hsweb.web.core.datasource.DynamicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
        dataSourceBean.setMinPoolSize(5);
        dataSourceBean.setMaxPoolSize(200);
        return dataSourceBean;
    }

    @Bean(name = "dynamicDataSource")
    public DynamicXaDataSourceImpl dynamicXaDataSource(@Qualifier("dataSource") DataSource dataSource) {
        return new DynamicXaDataSourceImpl(dataSource);
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
