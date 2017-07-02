/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.dao.mybatis.dynamic;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.transaction.Transaction;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.mybatis.spring.transaction.SpringManagedTransaction;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hswebframework.web.datasource.DataSourceHolder.switcher;

/**
 * mybatis 同一事务，同一个mapper，动态数据源切换支持
 *
 * @author zhouhao
 */
public class DynamicSpringManagedTransaction implements Transaction {

    private static final Log LOGGER = LogFactory.getLog(SpringManagedTransaction.class);

    private Map<String, TransactionProxy> connectionMap = new HashMap<>();

    /**
     * 当前数据源对应的事务代理
     *
     * @return {@link TransactionProxy}
     */
    protected TransactionProxy getProxy() {
        return connectionMap.get(switcher().currentDataSourceId());
    }

    /**
     * 添加一个事务代理
     *
     * @param proxy
     */
    protected void addProxy(TransactionProxy proxy) {
        connectionMap.put(switcher().currentDataSourceId(), proxy);
    }

    /**
     * 获取所有代理
     *
     * @return
     */
    protected Collection<TransactionProxy> getAllProxy() {
        return connectionMap.values();
    }

    @Override
    public Connection getConnection() throws SQLException {
        TransactionProxy proxy = getProxy();
        if (proxy != null) {
            return proxy.getConnection();
        }
        //根据当前激活的数据源 获取jdbc链接
        DataSource dataSource = DataSourceHolder.currentDataSource().getNative();
        String dsId = switcher().currentDataSourceId();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        proxy = new TransactionProxy(dsId, connection, dataSource);
        addProxy(proxy);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "DataSource (" + dsId + ") JDBC Connection ["
                            + connection
                            + "] will"
                            + (proxy.isConnectionTransactional ? " " : " not ")
                            + "be managed by Spring");
        }

        return connection;
    }

    @Override
    public void commit() throws SQLException {
        for (TransactionProxy proxy : getAllProxy()) {
            proxy.commit();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() throws SQLException {
        for (TransactionProxy proxy : getAllProxy()) {
            proxy.rollback();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws SQLException {
        SQLException tmp = null;
        for (TransactionProxy proxy : getAllProxy()) {
            try {
                proxy.close();
                //保证每个链接都能被释放
            } catch (SQLException e) {
                tmp = e;
            }
        }
        connectionMap.clear();
        if (null != tmp) throw tmp;
    }

    @Override
    public Integer getTimeout() throws SQLException {
        return getProxy().getTimeout();
    }

    class TransactionProxy implements Transaction {
        Connection connection;
        DataSource dataSource;
        boolean    isConnectionTransactional;
        boolean    autoCommit;
        String     dataSourceId;

        public TransactionProxy(String dataSourceId, Connection connection, DataSource dataSource) {
            this.connection = connection;
            this.dataSource = dataSource;
            this.dataSourceId = dataSourceId;
            this.isConnectionTransactional = DataSourceUtils.isConnectionTransactional(connection, dataSource);
            try {
                this.autoCommit = connection.getAutoCommit();
            } catch (SQLException e) {
            }
        }

        @Override
        public Connection getConnection() throws SQLException {
            return connection;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void commit() throws SQLException {
            if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Committing DataSource (" + dataSourceId + ") JDBC Connection [" + this.connection + "]");
                }
                this.connection.commit();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void rollback() throws SQLException {
            if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Rolling back DataSource (" + dataSourceId + ") JDBC Connection [" + this.connection + "]");
                }
                this.connection.rollback();
            }
        }

        @Override
        public void close() throws SQLException {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }

        @Override
        public Integer getTimeout() throws SQLException {
            ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
            if (holder != null && holder.hasTimeout()) {
                return holder.getTimeToLiveInSeconds();
            }
            return null;
        }
    }
}
