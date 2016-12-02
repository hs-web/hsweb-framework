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

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.hsweb.web.core.datasource.DatabaseType;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jta.atomikos.AtomikosProperties;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.Properties;

/**
 * @author zhouhao
 * @see com.atomikos.jdbc.AbstractDataSourceBean
 * @since 2.1
 */
@ConfigurationProperties(prefix = "hsweb.dynamicDatasource")
public class DynamicDataSourceProperties
        implements BeanClassLoaderAware, InitializingBean {
    private String                name                    = "core";
    private DatabaseType          type                    = null;
    private String                datasourceName          = null;
    private String                username                = "sa";
    private String                password                = "";
    private String                url                     = "jdbc:h2:file:./data/h2db;DB_CLOSE_ON_EXIT=FALSE";
    private String                testQuery               = null;
    private int                   loginTimeout            = 0;
    private int                   maxLifetime             = 0;
    private int                   minPoolSize             = 3;
    private int                   maxPoolSize             = 80;
    private int                   borrowConnectionTimeout = 60;
    private int                   reapTimeout             = 0;
    private int                   maxIdleTime             = 60;
    private int                   maintenanceInterval     = 60;
    private int                   defaultIsolationLevel   = -1;
    private int                   transactionTimeout      = 300;
    private Properties            properties              = null;
    private ClassLoader           classLoader             = null;
    private DatasourceTypeSupport datasourceTypeSupport   = null;
    private AtomikosProperties    icatch      = new AtomikosProperties();

    public int getTransactionTimeout() {
        return transactionTimeout;
    }

    public void setTransactionTimeout(int transactionTimeout) {
        this.transactionTimeout = transactionTimeout;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatabaseType getType() {
        if (type == null) {
            type = DatabaseType.fromJdbcUrl(getUrl());
        }
        return type;
    }

    public AtomikosProperties getIcatch() {
        return icatch;
    }

    public void setIcatch(AtomikosProperties icatch) {
        this.icatch = icatch;
    }

    public void setType(DatabaseType type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTestQuery() {
        return testQuery;
    }

    public void setTestQuery(String testQuery) {
        this.testQuery = testQuery;
    }

    public int getLoginTimeout() {
        return loginTimeout;
    }

    public void setLoginTimeout(int loginTimeout) {
        this.loginTimeout = loginTimeout;
    }

    public int getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(int maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getBorrowConnectionTimeout() {
        return borrowConnectionTimeout;
    }

    public void setBorrowConnectionTimeout(int borrowConnectionTimeout) {
        this.borrowConnectionTimeout = borrowConnectionTimeout;
    }

    public int getReapTimeout() {
        return reapTimeout;
    }

    public void setReapTimeout(int reapTimeout) {
        this.reapTimeout = reapTimeout;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public int getMaintenanceInterval() {
        return maintenanceInterval;
    }

    public void setMaintenanceInterval(int maintenanceInterval) {
        this.maintenanceInterval = maintenanceInterval;
    }

    public int getDefaultIsolationLevel() {
        return defaultIsolationLevel;
    }

    public void setDefaultIsolationLevel(int defaultIsolationLevel) {
        this.defaultIsolationLevel = defaultIsolationLevel;
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(url);
        Assert.notNull(username);
        if (datasourceName == null) {
            datasourceName = lookupSupportDatasourceName();
        }
        if (type == null) {
            type = DatabaseType.fromJdbcUrl(getUrl());
        }
        if (!StringUtils.hasText(testQuery)) testQuery = getType().getTestQuery();
        getProperties().put(datasourceTypeSupport.usernameProperty, getUsername());
        getProperties().put(datasourceTypeSupport.passwordProperty, getPassword());
        getProperties().put(datasourceTypeSupport.urlProperty, getUrl());
        initDefaultProperties();
    }

    public String lookupSupportDatasourceName() throws ClassNotFoundException {
        for (DatasourceTypeSupport support : DatasourceTypeSupport.values()) {
            try {
                ClassUtils.forName(support.className, classLoader);
                datasourceTypeSupport = support;
                return support.className;
            } catch (ClassNotFoundException e) {
            }
        }
        return getType().getXaDataSourceClassName();
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void putProperties(AtomikosDataSourceBean dataSourceBean) {
        dataSourceBean.setXaProperties(this.getProperties());
        dataSourceBean.setXaDataSourceClassName(this.getDatasourceName());
        dataSourceBean.setUniqueResourceName(this.getName());
        dataSourceBean.setMinPoolSize(this.getMinPoolSize());
        dataSourceBean.setMaxPoolSize(this.getMaxPoolSize());
        dataSourceBean.setTestQuery(this.getTestQuery());
        dataSourceBean.setBorrowConnectionTimeout(this.getBorrowConnectionTimeout());
        dataSourceBean.setMaintenanceInterval(this.getMaintenanceInterval());
        dataSourceBean.setDefaultIsolationLevel(this.getDefaultIsolationLevel());
        dataSourceBean.setMaxLifetime(this.getMaxLifetime());
        dataSourceBean.setMaxIdleTime(this.getMaxIdleTime());
        dataSourceBean.setReapTimeout(this.getReapTimeout());
        try {
            dataSourceBean.setLoginTimeout(this.getLoginTimeout());
        } catch (SQLException e) {
        }
    }

    public void initDefaultProperties() {
        datasourceTypeSupport.putDefaultProperties(getProperties());
    }

    private enum DatasourceTypeSupport {
        druid("com.alibaba.druid.pool.xa.DruidXADataSource", "username", "password", "url") {
            @Override
            public void putDefaultProperties(Properties properties) {
                super.putDefaultProperties(properties);
                properties.putIfAbsent("filters", "stat");
                properties.putIfAbsent("maxActive", 200);
                properties.putIfAbsent("initialSize", 3);
                properties.putIfAbsent("minIdle", 3);
                properties.putIfAbsent("maxWait", 5000);
                properties.putIfAbsent("timeBetweenEvictionRunsMillis", 60000);
                properties.putIfAbsent("minEvictableIdleTimeMillis", 1800000);
                properties.putIfAbsent("testWhileIdle", true);
                properties.putIfAbsent("testOnBorrow", false);
                properties.putIfAbsent("testOnReturn", false);
                properties.putIfAbsent("poolPreparedStatements", true);
                properties.putIfAbsent("maxOpenPreparedStatements", 20);
            }
        };

        DatasourceTypeSupport(String className, String usernameProperty, String passwordProperty, String urlProperty) {
            this.className = className;
            this.usernameProperty = usernameProperty;
            this.passwordProperty = passwordProperty;
            this.urlProperty = urlProperty;
        }

        final String className;
        final String usernameProperty;
        final String passwordProperty;
        final String urlProperty;

        public void putDefaultProperties(Properties properties) {

        }
    }
}
