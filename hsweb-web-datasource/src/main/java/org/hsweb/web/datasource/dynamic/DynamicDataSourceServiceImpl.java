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
import org.hsweb.web.core.datasource.DatabaseType;
import org.hsweb.web.core.datasource.DynamicDataSource;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.service.datasource.DataSourceService;
import org.hsweb.web.service.datasource.DynamicDataSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
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
    public String getDataBaseType(String id) {
        return getCache(id).getDatabaseType().name();
    }

    @Override
    @PreDestroy
    public void destroyAll() throws Exception {
        cache.values().stream().map(CacheInfo::getDataSource).forEach(this::closeDataSource);
        cache.clear();
    }

    protected void closeDataSource(javax.sql.CommonDataSource ds) {
        if (ds instanceof AtomikosDataSourceBean) {
            closeDataSource(((AtomikosDataSourceBean) ds).getXaDataSource());
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
            ReadWriteLock readWriteLock = lockFactory.createReadWriteLock("dynamic.ds." + id);

            readWriteLock.readLock().tryLock();
            CacheInfo cacheInfo = null;
            try {
                cacheInfo = cache.get(id);
                // 缓存存在,并且hash一致
                if (cacheInfo != null && cacheInfo.getHash() == old.getHash())
                    return cacheInfo;
            } finally {
                try {
                    readWriteLock.readLock().unlock();
                } catch (Exception e) {
                }
            }
            readWriteLock.writeLock().tryLock();
            try {
                if (cacheInfo != null) {
                    closeDataSource(cacheInfo.getDataSource());
                }
                //加载datasource到缓存
                javax.sql.DataSource dataSource = createDataSource(old);
                DatabaseType databaseType = DatabaseType.fromJdbcUrl(old.getUrl());
                cacheInfo = new CacheInfo(old.getHash(), dataSource, databaseType);
                cache.put(id, cacheInfo);
            } finally {
                try {
                    readWriteLock.writeLock().unlock();
                } catch (Exception e) {
                }
            }
            return cacheInfo;
        } finally {
            DynamicDataSource.useLast();
        }
    }

    protected javax.sql.DataSource createDataSource(DataSource dataSource) {
        DynamicDataSourceProperties properties = new DynamicDataSourceProperties();
        properties.setName("ds_" + dataSource.getId());
        properties.setBeanClassLoader(this.getClass().getClassLoader());
        properties.setUsername(dataSource.getUsername());
        properties.setPassword(dataSource.getPassword());
        properties.setUrl(dataSource.getUrl());
        properties.setType(DatabaseType.fromJdbcUrl(dataSource.getUrl()));
        properties.setTestQuery(dataSource.getTestSql());
        Map<String, Object> otherProperties = dataSource.getProperties();
        try {
            properties.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (otherProperties != null) {
            properties.getProperties().putAll(otherProperties);
        } else {
            properties.initDefaultProperties();
        }

        AtomikosDataSourceBean dataSourceBean = new AtomikosDataSourceBean();
        properties.putProperties(dataSourceBean);
        boolean[] success = new boolean[1];
        //异步初始化
        new Thread(() -> {
            try {
                dataSourceBean.init();
                success[0] = true;
            } catch (AtomikosSQLException e) {
                logger.error("创建数据源失败", e);
                closeDataSource(dataSourceBean);
                cache.remove(dataSource.getId());
            }
        }).start();
        //初始化检测
        new Thread(() -> {
            try {
                Thread.sleep(30 * 1000);
                if (!success[0]) {
                    logger.error("初始化jdbc超时:{}", dataSourceBean);
                    closeDataSource(dataSourceBean);
                    cache.remove(dataSource.getId());
                }
            } catch (Exception e) {

            }
        }).start();
        return dataSourceBean;
    }

    @PostConstruct
    public void init() {
        if (null != dynamicDataSource && dynamicDataSource instanceof DynamicXaDataSourceImpl)
            ((DynamicXaDataSourceImpl) dynamicDataSource).setDynamicDataSourceService(this);
    }

    class CacheInfo {
        int                  hash;
        DatabaseType         databaseType;
        javax.sql.DataSource dataSource;

        public CacheInfo(int hash, javax.sql.DataSource dataSource, DatabaseType type) {
            this.hash = hash;
            this.dataSource = dataSource;
            this.databaseType = type;
        }

        public int getHash() {
            return hash;
        }

        public javax.sql.DataSource getDataSource() {
            return dataSource;
        }

        public DatabaseType getDatabaseType() {
            return databaseType;
        }
    }

}
