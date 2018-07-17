package org.hswebframework.web.service;

import org.hswebframework.web.commons.entity.GenericEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.List;

/**
 * @author zhouhao
 * @see org.springframework.cache.annotation.CacheConfig
 * @see Cacheable
 * @see CacheEvict
 * @since 3.0
 */
public abstract class EnableCacheAllEvictGenericEntityService<E extends GenericEntity<PK>, PK> extends GenericEntityService<E, PK> {

    @Override
    @Cacheable(key = "'id:'+#pk")
    public E selectByPk(PK pk) {
        return super.selectByPk(pk);
    }

    @Override
    @CacheEvict(allEntries = true)
    public int updateByPk(List<E> data) {
        return super.updateByPk(data);
    }

    @Override
    @CacheEvict(allEntries = true)
    public int updateByPk(PK pk, E entity) {
        return super.updateByPk(pk, entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    public PK insert(E entity) {
        return super.insert(entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    public E deleteByPk(PK pk) {
        return super.deleteByPk(pk);
    }

    @Override
    @CacheEvict(allEntries = true)
    public PK saveOrUpdate(E entity) {
        return super.saveOrUpdate(entity);
    }

    @Override
    @Cacheable(key = "'all'")
    public List<E> select() {
        return super.select();
    }

    @Override
    @Cacheable(key = "'id-in:'+#id.hashCode()")
    public List<E> selectByPk(List<PK> id) {
        return super.selectByPk(id);
    }

    @Override
    @Cacheable(key = "'count'")
    public int count() {
        return super.count();
    }
}
