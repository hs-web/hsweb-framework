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
import com.atomikos.jdbc.AtomikosSQLException;
import org.hsweb.concurrent.lock.LockFactory;
import org.hsweb.web.bean.po.datasource.DataSource;
import org.hsweb.web.core.datasource.DynamicDataSource;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.service.datasource.DataSourceService;
import org.hsweb.web.service.datasource.DynamicDataSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;

@Service("dynamicDataSourceService")
public class DynamicDataSourceServiceImpl implements DynamicDataSourceService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private DataSourceService dataSourceService;

    @Autowired(required = false)
    protected DynamicDataSource dynamicDataSource;

    @Autowired
    private LockFactory lockFactory;

    private ConcurrentMap<String, CacheInfo> cache = new ConcurrentHashMap<>();

    @Override
    public javax.sql.DataSource getDataSource(String id) {
        return getCache(id).getDataSource();
    }

    @Override
    @PreDestroy
    public void destroyAll() throws Exception {
        cache.values().stream().map(CacheInfo::getDataSource).forEach(this::closeDataSource);
    }

    protected void closeDataSource(javax.sql.DataSource ds) {
        if (ds instanceof AtomikosDataSourceBean) {
            ((AtomikosDataSourceBean) ds).close();
        } else if (ds instanceof Closeable) {
            try {
                ((Closeable) ds).close();
            } catch (IOException e) {
                logger.error("close datasource error", e);
            }
        }
    }

    protected CacheInfo getCache(String id) {
        DynamicDataSource.useDefault();
        try {
            DataSource old = dataSourceService.selectByPk(id);
            if (old == null || old.getEnabled() != 1) throw new NotFoundException("数据源不存在或已禁用");
            //创建锁
            ReadWriteLock readWriteLock = lockFactory.createReadWriteLock("datasource.lock." + id);
            readWriteLock.readLock().tryLock();
            try {
                CacheInfo cacheInfo = cache.get(id);
                // 缓存存在,并且hash一致
                if (cacheInfo != null && cacheInfo.getHash() == old.getHash())
                    return cacheInfo;
            } finally {
                readWriteLock.readLock().unlock();
            }
            //加载datasource到缓存
            readWriteLock.writeLock().tryLock();
            try {
                javax.sql.DataSource dataSource = createDataSource(old);

                CacheInfo cacheInfo = new CacheInfo(old.getHash(), dataSource);
                CacheInfo oldCache = cache.put(id, cacheInfo);
                if (oldCache != null) {
                    closeDataSource(oldCache.getDataSource());
                }
                return cacheInfo;
            } finally {
                readWriteLock.writeLock().unlock();
            }
        } finally {
            DynamicDataSource.useLast();
        }
    }

    @Autowired
    private DataSourceProperties properties;

    protected javax.sql.DataSource createDataSource(DataSource dataSource) {
        AtomikosDataSourceBean dataSourceBean = new AtomikosDataSourceBean();
        Properties xaProperties = new Properties();
        if (dataSource.getProperties() != null)
            xaProperties.putAll(dataSource.getProperties());
        if (dataSource.getDriver().contains("mysql")) {
            dataSourceBean.setXaDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
            xaProperties.put("pinGlobalTxToPhysicalConnection", true);
            xaProperties.put("user", dataSource.getUsername());
            xaProperties.put("password", dataSource.getPassword());
            xaProperties.put("url", dataSource.getUrl());
        } else {
            dataSourceBean.setXaDataSourceClassName(properties.getXa().getDataSourceClassName());
            xaProperties.put("username", dataSource.getUsername());
            xaProperties.put("password", dataSource.getPassword());
            xaProperties.put("url", dataSource.getUrl());
            xaProperties.put("driverClassName", dataSource.getDriver());
        }
        dataSourceBean.setXaProperties(xaProperties);
        dataSourceBean.setUniqueResourceName("ds_" + dataSource.getId());
        dataSourceBean.setMaxPoolSize(200);
        dataSourceBean.setMinPoolSize(5);
        dataSourceBean.setTestQuery(dataSource.getTestSql());
        dataSourceBean.setBorrowConnectionTimeout(60);
        try {
            dataSourceBean.init();
        } catch (AtomikosSQLException e) {
            dataSourceBean.close();
            throw new RuntimeException(e);
        }
        return dataSourceBean;
    }

    @PostConstruct
    public void init() {
        if (null != dynamicDataSource)
            ((DynamicXaDataSourceImpl) dynamicDataSource).setDynamicDataSourceService(this);
    }

    class CacheInfo {
        int hash;

        javax.sql.DataSource dataSource;

        public CacheInfo(int hash, javax.sql.DataSource dataSource) {
            this.hash = hash;
            this.dataSource = dataSource;
        }

        public int getHash() {
            return hash;
        }

        public javax.sql.DataSource getDataSource() {
            return dataSource;
        }
    }

}
