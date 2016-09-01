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

package org.hsweb.web.service.impl.datasource;

import org.hsweb.web.core.datasource.DynamicDataSource;
import org.hsweb.web.service.datasource.DynamicDataSourceService;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DynamicDataSourceImpl extends AbstractDataSource implements DynamicDataSource {
    private javax.sql.DataSource defaultDataSource;

    protected DynamicDataSourceService dynamicDataSourceService;

    public DynamicDataSourceImpl(javax.sql.DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
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
        if (sourceId == null || dynamicDataSourceService == null) return defaultDataSource;
        DataSource dataSource = dynamicDataSourceService.getDataSource(sourceId);
        if (dataSource == null) return defaultDataSource;
        return dataSource;
    }

    public void setDefaultDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    public void setDynamicDataSourceService(DynamicDataSourceService dynamicDataSourceService) {
        this.dynamicDataSourceService = dynamicDataSourceService;
    }

}
