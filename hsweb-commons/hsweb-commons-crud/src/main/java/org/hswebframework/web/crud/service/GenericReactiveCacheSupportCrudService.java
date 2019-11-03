package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.cache.ReactiveCache;
import org.hswebframework.web.cache.ReactiveCacheManager;
import org.hswebframework.web.cache.supports.UnSupportedReactiveCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;

public abstract class GenericReactiveCacheSupportCrudService<E, K> implements EnableCacheReactiveCrudService<E, K> {

    @Autowired
    private ReactiveRepository<E, K> repository;

    @Override
    public ReactiveRepository<E, K> getRepository() {
        return repository;
    }

    @Autowired(required = false)
    private ReactiveCacheManager cacheManager;

    protected ReactiveCache<E> cache;

    @Override
    public ReactiveCache<E> getCache() {
        if (cache != null) {
            return cache;
        }
        if (cacheManager == null) {
            return cache = UnSupportedReactiveCache.getInstance();
        }

        return cache = cacheManager.getCache(getCacheName());
    }

    public String getCacheName() {
        return this.getClass().getSimpleName();
    }
}
