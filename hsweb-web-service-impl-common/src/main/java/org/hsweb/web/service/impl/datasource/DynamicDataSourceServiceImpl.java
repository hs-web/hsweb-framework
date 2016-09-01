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

import org.hsweb.concurrent.lock.LockFactory;
import org.hsweb.ezorm.executor.SqlExecutor;
import org.hsweb.web.bean.po.datasource.DataSource;
import org.hsweb.web.core.datasource.DynamicDataSource;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.service.datasource.DataSourceService;
import org.hsweb.web.service.datasource.DynamicDataSourceService;
import org.hsweb.web.service.impl.basic.SqlExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;

@Service("dynamicDataSourceService")
public class DynamicDataSourceServiceImpl implements DynamicDataSourceService {

    @Resource
    private DataSourceService dataSourceService;

    @Autowired(required = false)
    private DynamicDataSource dynamicDataSource;

    @Autowired
    private LockFactory lockFactory;

    private ConcurrentMap<String, CacheInfo> cache = new ConcurrentHashMap<>();

    @Override
    public javax.sql.DataSource getDataSource(String id) {
        return getCache(id).getDataSource();
    }

    public PlatformTransactionManager getTransactionManager(String id) {
        return getCache(id).getTransactionManager();
    }

    @Override
    public SqlExecutor getSqlExecutor(String id) {
        return getCache(id).getSqlExecutor();
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
                javax.sql.DataSource dataSource = dataSourceService.createDataSource(id);
                CacheInfo cacheInfo = new CacheInfo(old.getHash(), dataSource);
                cache.put(id, cacheInfo);
                return cacheInfo;
            } finally {
                readWriteLock.writeLock().unlock();
            }
        } finally {
            DynamicDataSource.useLast();
        }
    }

    @PostConstruct
    public void init() {
        if (null != dynamicDataSource)
            ((DynamicDataSourceImpl) dynamicDataSource).setDynamicDataSourceService(this);
    }

    class CacheInfo {
        int hash;

        javax.sql.DataSource dataSource;

        PlatformTransactionManager transactionManager;

        SqlExecutor sqlExecutor;

        public CacheInfo(int hash, javax.sql.DataSource dataSource) {
            this.hash = hash;
            this.dataSource = dataSource;
            sqlExecutor = new SqlExecutorService().setDataSource(dataSource);
            transactionManager = new DataSourceTransactionManager(dataSource);
        }

        public int getHash() {
            return hash;
        }

        public javax.sql.DataSource getDataSource() {
            return dataSource;
        }

        public SqlExecutor getSqlExecutor() {
            return sqlExecutor;
        }

        public PlatformTransactionManager getTransactionManager() {
            return transactionManager;
        }
    }

}
