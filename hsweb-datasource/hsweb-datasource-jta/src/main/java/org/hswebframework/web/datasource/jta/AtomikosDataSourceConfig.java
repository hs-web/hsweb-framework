package org.hswebframework.web.datasource.jta;

import com.atomikos.jdbc.AtomikosDataSourceBean;

import java.sql.SQLException;
import java.util.Properties;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class AtomikosDataSourceConfig {
    private int        minPoolSize             = 5;
    private int        maxPoolSize             = 200;
    private int        borrowConnectionTimeout = 60;
    private int        reapTimeout             = 0;
    private int        maxIdleTime             = 60;
    private int        maintenanceInterval     = 60;
    private int        defaultIsolationLevel   = -1;
    private String     xaDataSourceClassName   = null;
    private int        loginTimeout            = 0;
    private String     testQuery               = null;
    private int        maxLifetime             = 0;
    private Properties xaProperties            = null;
    //初始化超时时间
    private int        initTimeout             = 10;

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AtomikosDataSourceConfig && hashCode() == obj.hashCode();
    }

    @Override
    public String toString() {
        return "AtomikosDataSourceConfig{" +
                "minPoolSize=" + minPoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", borrowConnectionTimeout=" + borrowConnectionTimeout +
                ", reapTimeout=" + reapTimeout +
                ", maxIdleTime=" + maxIdleTime +
                ", maintenanceInterval=" + maintenanceInterval +
                ", defaultIsolationLevel=" + defaultIsolationLevel +
                ", xaDataSourceClassName='" + xaDataSourceClassName + '\'' +
                ", loginTimeout=" + loginTimeout +
                ", testQuery='" + testQuery + '\'' +
                ", maxLifetime=" + maxLifetime +
                ", xaProperties=" + xaProperties +
                ", initTimeout=" + initTimeout +
                '}';
    }

    public Properties getXaProperties() {
        return xaProperties;
    }

    public void setXaProperties(Properties xaProperties) {
        this.xaProperties = xaProperties;
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

    public String getXaDataSourceClassName() {
        return xaDataSourceClassName;
    }

    public void setXaDataSourceClassName(String xaDataSourceClassName) {
        this.xaDataSourceClassName = xaDataSourceClassName;
    }

    public int getLoginTimeout() {
        return loginTimeout;
    }

    public void setLoginTimeout(int loginTimeout) {
        this.loginTimeout = loginTimeout;
    }

    public String getTestQuery() {
        return testQuery;
    }

    public void setTestQuery(String testQuery) {
        this.testQuery = testQuery;
    }

    public int getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(int maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public int getInitTimeout() {
        return initTimeout;
    }

    public void setInitTimeout(int initTimeout) {
        this.initTimeout = initTimeout;
    }

    public void putProperties(AtomikosDataSourceBean atomikosDataSourceBean) {
        if (null != xaProperties) {
            xaProperties.entrySet().forEach(entry -> entry.setValue(String.valueOf(entry.getValue())));
        }
        atomikosDataSourceBean.setXaDataSourceClassName(getXaDataSourceClassName());
        atomikosDataSourceBean.setBorrowConnectionTimeout(getBorrowConnectionTimeout());
        if (loginTimeout != 0) {
            try {
                atomikosDataSourceBean.setLoginTimeout(getLoginTimeout());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        atomikosDataSourceBean.setMaxIdleTime(getMaxIdleTime());
        atomikosDataSourceBean.setMaxPoolSize(getMaxPoolSize());
        atomikosDataSourceBean.setMinPoolSize(getMinPoolSize());
        atomikosDataSourceBean.setDefaultIsolationLevel(getDefaultIsolationLevel());
        atomikosDataSourceBean.setMaintenanceInterval(getMaintenanceInterval());
        atomikosDataSourceBean.setReapTimeout(getReapTimeout());
        atomikosDataSourceBean.setTestQuery(getTestQuery());
        atomikosDataSourceBean.setXaProperties(getXaProperties());
        atomikosDataSourceBean.setMaxLifetime(getMaxLifetime());
    }
}
