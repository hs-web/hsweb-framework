/**
 * Copyright 2010-2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hsweb.web.mybatis.dynamic;

import static org.springframework.util.Assert.notNull;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.hsweb.web.core.datasource.DataSourceHolder;
import org.mybatis.spring.SqlSessionHolder;
import org.mybatis.spring.SqlSessionUtils;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles MyBatis SqlSession life cycle. It can register and get SqlSessions from
 * Spring {@code TransactionSynchronizationManager}. Also works if no transaction is active.
 *
 * @author Hunter Presnall
 * @author Eduardo Macarron
 * @version $Id$
 */
public final class DynamicSqlSessionUtils {

    private static final Log LOGGER = LogFactory.getLog(DynamicSqlSessionUtils.class);

    private static final String NO_EXECUTOR_TYPE_SPECIFIED       = "No ExecutorType specified";
    private static final String NO_SQL_SESSION_FACTORY_SPECIFIED = "No SqlSessionFactory specified";
    private static final String NO_SQL_SESSION_SPECIFIED         = "No SqlSession specified";

    /**
     * This class can't be instantiated, exposes static utility methods only.
     */
    private DynamicSqlSessionUtils() {
        // do nothing
    }

    /**
     * Creates a new MyBatis {@code SqlSession} from the {@code SqlSessionFactory}
     * provided as a parameter and using its {@code DataSource} and {@code ExecutorType}
     *
     * @param sessionFactory a MyBatis {@code SqlSessionFactory} to create new sessions
     * @return a MyBatis {@code SqlSession}
     * @throws TransientDataAccessResourceException if a transaction is active and the
     *                                              {@code SqlSessionFactory} is not using a {@code SpringManagedTransactionFactory}
     */
    public static SqlSession getSqlSession(SqlSessionFactory sessionFactory) {
        ExecutorType executorType = sessionFactory.getConfiguration().getDefaultExecutorType();
        return getSqlSession(sessionFactory, executorType, null);
    }

    static final String SQL_SESSION_RESOURCE_KEY = "dynamic-sqlSession";

    /**
     * Gets an SqlSession from Spring Transaction Manager or creates a new one if needed.
     * Tries to get a SqlSession out of current transaction. If there is not any, it creates a new one.
     * Then, it synchronizes the SqlSession with the transaction if Spring TX is active and
     * <code>SpringManagedTransactionFactory</code> is configured as a transaction manager.
     *
     * @param sessionFactory      a MyBatis {@code SqlSessionFactory} to create new sessions
     * @param executorType        The executor type of the SqlSession to create
     * @param exceptionTranslator Optional. Translates SqlSession.commit() exceptions to Spring exceptions.
     * @throws TransientDataAccessResourceException if a transaction is active and the
     *                                              {@code SqlSessionFactory} is not using a {@code SpringManagedTransactionFactory}
     * @see SpringManagedTransactionFactory
     */
    public static SqlSession getSqlSession(SqlSessionFactory sessionFactory, ExecutorType executorType, PersistenceExceptionTranslator exceptionTranslator) {

        notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);
        notNull(executorType, NO_EXECUTOR_TYPE_SPECIFIED);
        DynamicSqlSessionHolder holder = (DynamicSqlSessionHolder) TransactionSynchronizationManager.getResource(SQL_SESSION_RESOURCE_KEY);
        if (holder == null) {
            TransactionSynchronizationManager.bindResource(SQL_SESSION_RESOURCE_KEY, holder = new DynamicSqlSessionHolder());
        }
        SqlSession session = holder.getSqlSession();
        if (session != null) {
            return session;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating a new SqlSession for datasource " + holder.getDataSourceId());
        }

        session = sessionFactory.openSession(executorType);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new SqlSessionSynchronization(session, holder));
            if (!holder.isSynchronizedWithTransaction()) {
                holder.setSynchronizedWithTransaction(true);
                holder.requested();
            }

        }
        holder.setSqlSession(session);
        return session;
    }

    /**
     * Checks if {@code SqlSession} passed as an argument is managed by Spring {@code TransactionSynchronizationManager}
     * If it is not, it closes it, otherwise it just updates the reference counter and
     * lets Spring call the close callback when the managed transaction ends
     *
     * @param session
     * @param sessionFactory
     */
    public static void closeSqlSession(SqlSession session, SqlSessionFactory sessionFactory) {
        notNull(session, NO_SQL_SESSION_SPECIFIED);
        notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);
        DynamicSqlSessionHolder holder = (DynamicSqlSessionHolder) TransactionSynchronizationManager.getResource(SQL_SESSION_RESOURCE_KEY);
        if ((holder != null) && (holder.getAllSqlSession().contains(session))) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Releasing transactional SqlSession [" + session + "]");
            }
            holder.released();
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Closing non transactional SqlSession [" + session + "]");
            }
            session.close();
        }
    }

    /**
     * Returns if the {@code SqlSession} passed as an argument is being managed by Spring
     *
     * @param session        a MyBatis SqlSession to check
     * @param sessionFactory the SqlSessionFactory which the SqlSession was built with
     * @return true if session is transactional, otherwise false
     */
    public static boolean isSqlSessionTransactional(SqlSession session, SqlSessionFactory sessionFactory) {
        notNull(session, NO_SQL_SESSION_SPECIFIED);
        notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);
        DynamicSqlSessionHolder holder = (DynamicSqlSessionHolder) TransactionSynchronizationManager.getResource(SQL_SESSION_RESOURCE_KEY);

        return (holder != null) && (holder.getAllSqlSession().contains(session));
    }

    /**
     * Callback for cleaning up resources. It cleans TransactionSynchronizationManager and
     * also commits and closes the {@code SqlSession}.
     * It assumes that {@code Connection} life cycle will be managed by
     * {@code DataSourceTransactionManager} or {@code JtaTransactionManager}
     */
    private static final class SqlSessionSynchronization extends TransactionSynchronizationAdapter {

        private final DynamicSqlSessionHolder holder;

        private boolean holderActive = true;

        private SqlSession sqlSession;

        private String dataSourceId;

        public SqlSessionSynchronization(SqlSession sqlSession, DynamicSqlSessionHolder holder) {
            notNull(holder, "Parameter 'holder' must be not null");
            this.holder = holder;
            this.sqlSession = sqlSession;
            this.dataSourceId = holder.getDataSourceId();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getOrder() {
            // order right before any Connection synchronization
            return DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void suspend() {
            if (this.holderActive) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transaction synchronization suspending SqlSession [" + sqlSession + "] for dataSource :" + dataSourceId);
                }
                holder.remove(dataSourceId);
                if (holder.getAllSqlSession().isEmpty() && TransactionSynchronizationManager.getResource(SQL_SESSION_RESOURCE_KEY) != null)
                    TransactionSynchronizationManager.unbindResource(SQL_SESSION_RESOURCE_KEY);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void resume() {
            if (this.holderActive) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transaction synchronization resuming SqlSession [" + sqlSession + "] for dataSource :" + dataSourceId);
                }
                holder.setSqlSession(dataSourceId, sqlSession);
                if (TransactionSynchronizationManager.getResource(SQL_SESSION_RESOURCE_KEY) == null)
                    TransactionSynchronizationManager.bindResource(SQL_SESSION_RESOURCE_KEY, this.holder);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void beforeCommit(boolean readOnly) {
            // Connection commit or rollback will be handled by ConnectionSynchronization or
            // DataSourceTransactionManager.
            // But, do cleanup the SqlSession / Executor, including flushing BATCH statements so
            // they are actually executed.
            // SpringManagedTransaction will no-op the commit over the jdbc connection
            // TODO This updates 2nd level caches but the tx may be rolledback later on!
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                try {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Transaction synchronization committing SqlSession [" + sqlSession + "] for dataSource :" + dataSourceId);
                    }
                    sqlSession.commit();
                } catch (PersistenceException p) {
                    throw p;
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void beforeCompletion() {
            // Issue #18 Close SqlSession and deregister it now
            // because afterCompletion may be called from a different thread
            if (!this.holder.isOpen()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transaction synchronization deregistering SqlSession [" + sqlSession + "] for dataSource :" + dataSourceId);
                }
                holder.remove(dataSourceId);
                if (holder.getAllSqlSession().isEmpty() && TransactionSynchronizationManager.getResource(SQL_SESSION_RESOURCE_KEY) != null)
                    TransactionSynchronizationManager.unbindResource(SQL_SESSION_RESOURCE_KEY);
                this.holderActive = false;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transaction synchronization closing SqlSession [" + sqlSession + "] for dataSource :" + dataSourceId);
                }
                sqlSession.close();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void afterCompletion(int status) {
            if (this.holderActive) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transaction synchronization deregistering SqlSession [" + sqlSession + "] for dataSource :" + dataSourceId);
                }
                holder.remove(dataSourceId);
                if (holder.getAllSqlSession().isEmpty() && TransactionSynchronizationManager.getResource(SQL_SESSION_RESOURCE_KEY) != null)
                    TransactionSynchronizationManager.unbindResource(SQL_SESSION_RESOURCE_KEY);
                this.holderActive = false;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transaction synchronization closing SqlSession [" + sqlSession + "] for dataSource :" + dataSourceId);
                }
                sqlSession.close();
            }
            //this.holder.reset();
        }
    }

}
