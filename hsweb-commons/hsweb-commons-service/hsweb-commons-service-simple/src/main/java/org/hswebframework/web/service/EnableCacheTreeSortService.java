package org.hswebframework.web.service;

import org.hswebframework.web.commons.entity.TreeSortSupportEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.Collection;
import java.util.List;

/**
 * @author zhouhao
 * @since 3.0
 */
public abstract class EnableCacheTreeSortService<E extends TreeSortSupportEntity<PK>, PK>
        extends AbstractTreeSortService<E, PK> {

    @Override
    @CacheEvict(allEntries = true)
    public int updateBatch(Collection<E> data) {
        return super.updateBatch(data);
    }

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
    @Caching(
            evict = {
                    @CacheEvict(key = "'id:'+#pk"),
                    @CacheEvict(key = "'all'"),
                    @CacheEvict(key = "'count'")
            }
    )
    public int updateByPk(PK pk, E entity) {
        return super.updateByPk(pk, entity);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(key = "'id:'+#result"),
                    @CacheEvict(key = "'all'"),
                    @CacheEvict(key = "'count'")
            }
    )
    public PK insert(E entity) {
        return super.insert(entity);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(key = "'id:'+#pk"),
                    @CacheEvict(key = "'all'"),
                    @CacheEvict(key = "'count'")
            }
    )
    public E deleteByPk(PK pk) {
        return super.deleteByPk(pk);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(key = "'id:'+#result"),
                    @CacheEvict(key = "'all'"),
                    @CacheEvict(key = "'count'")
            }
    )
    public PK saveOrUpdate(E entity) {
        return super.saveOrUpdate(entity);
    }

    @Override
    @Cacheable(key = "'all'")
    public List<E> select() {
        return super.select();
    }

    @Override
    @Cacheable(key = "'count'")
    public int count() {
        return super.count();
    }
}
