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
import org.hsweb.web.core.datasource.DynamicDataSource;
import org.hsweb.web.service.datasource.DynamicDataSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;

public class DynamicXaDataSourceImpl extends AbstractDataSource implements DynamicDataSource, XADataSource, Closeable {
    private Logger               logger            = LoggerFactory.getLogger(DynamicDataSource.class);
    private javax.sql.DataSource defaultDataSource = null;
    private   DatabaseType             defaultDatabaseType;
    protected DynamicDataSourceService dynamicDataSourceService;

    public DynamicXaDataSourceImpl(javax.sql.DataSource defaultDataSource, DatabaseType defaultDatabaseType) {
        Assert.notNull(defaultDataSource);
        Assert.notNull(defaultDatabaseType);
        this.defaultDataSource = defaultDataSource;
        this.defaultDatabaseType = defaultDatabaseType;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getActiveDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getActiveDataSource().getConnection(username, password);
    }

    public DataSource getActiveDataSource() {
        String sourceId = DynamicDataSource.getActiveDataSourceId();
        logger.info("use datasource:{}", sourceId == null ? "default" : sourceId);
        if (sourceId == null || dynamicDataSourceService == null) return defaultDataSource;
        DataSource dataSource = dynamicDataSourceService.getDataSource(sourceId);
        if (dataSource == null) {
            logger.info("use datasource:{} fail,because its not exists! use default datasource now.", sourceId);
            return defaultDataSource;
        }
        return dataSource;
    }

    @Override
    public DatabaseType getActiveDataBaseType() {
        String sourceId = DynamicDataSource.getActiveDataSourceId();
        if (sourceId == null || dynamicDataSourceService == null) return defaultDatabaseType;
        String type = dynamicDataSourceService.getDataBaseType(sourceId);
        if (type == null) return defaultDatabaseType;
        return DatabaseType.valueOf(type);
    }

    public XADataSource getActiveXADataSource() {
        DataSource activeDs = getActiveDataSource();
        XADataSource xaDataSource;
        if (activeDs instanceof XADataSource)
            xaDataSource = ((XADataSource) activeDs);
        else if (activeDs instanceof AtomikosDataSourceBean) {
            xaDataSource = ((AtomikosDataSourceBean) activeDs).getXaDataSource();
        } else {
            throw new UnsupportedOperationException(activeDs.getClass() + " is not XADataSource");
        }
        return xaDataSource;
    }

    public synchronized void setDynamicDataSourceService(DynamicDataSourceService dynamicDataSourceService) {
        if (this.dynamicDataSourceService != null) throw new UnsupportedOperationException();
        this.dynamicDataSourceService = dynamicDataSourceService;
    }

    @Override
    public XAConnection getXAConnection() throws SQLException {
        return getActiveXADataSource().getXAConnection();
    }

    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        return getActiveXADataSource().getXAConnection(user, password);
    }

    public void close() {
        try {
            if (dynamicDataSourceService != null)
                dynamicDataSourceService.destroyAll();
        } catch (Exception e) {
            logger.error("close datasource error", e);
        }
    }
}
