package org.hswebframework.web.service;

import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.cache.annotation.CacheConfig;

/**
 * @author zhouhao
 * @since 1.0
 */
@CacheConfig(cacheNames = "test-entity")
public class EnableCacheTestService extends EnableCacheGenericEntityService<TestEntity, String> {
    private CrudDao<TestEntity, String> dao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public CrudDao<TestEntity, String> getDao() {
        return dao;
    }

    public void setDao(CrudDao<TestEntity, String> dao) {
        this.dao = dao;
    }
}
