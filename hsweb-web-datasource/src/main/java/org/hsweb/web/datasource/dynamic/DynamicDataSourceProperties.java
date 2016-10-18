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

import com.atomikos.datasource.pool.ConnectionPool;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.hsweb.web.core.datasource.DatabaseType;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @author zhouhao
 * @see com.atomikos.jdbc.AbstractDataSourceBean
 * @since 2.1
 */
@ConfigurationProperties(prefix = "hsweb.dynamicDatasource")
public class DynamicDataSourceProperties
        implements BeanClassLoaderAware, InitializingBean {
    private static final List<String> supportDatasourceType;
    private String       name           = "core";
    private DatabaseType type           = null;
    private String       datasourceName = null;
    private String       username       = "sa";
    private String       password       = "";
    private String       url            = "jdbc:h2:file:./data/h2db;DB_CLOSE_ON_EXIT=FALSE";
    private String testQuery;
    private int         loginTimeout            = 0;
    private int         maxLifetime             = 0;
    private int         minPoolSize             = 2;
    private int         maxPoolSize             = 20;
    private int         borrowConnectionTimeout = 30;
    private int         reapTimeout             = 0;
    private int         maxIdleTime             = 60;
    private int         maintenanceInterval     = 60;
    private int         defaultIsolationLevel   = -1;
    private int         transactionTimeout      = 300;
    private Properties  properties              = null;
    private ClassLoader classLoader             = null;

    static {
        supportDatasourceType = new LinkedList<>();
        supportDatasourceType.add("com.alibaba.druid.pool.xa.DruidXADataSource");
    }

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
        properties.put("username", getUsername());
        properties.put("password", getPassword());
        properties.put("url", getUrl());
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
    }

    public String lookupSupportDatasourceName() throws ClassNotFoundException {
        for (String dsClass : supportDatasourceType) {
            try {
                ClassUtils.forName(dsClass, classLoader);
                return dsClass;
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

}
