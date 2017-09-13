package org.hswebframework.web.service;

import org.hswebframework.web.commons.entity.GenericEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.List;

/**
 * 启用缓冲的通用实体曾删改查服务,继承此类
 * 在类上注解{@link org.springframework.cache.annotation.CacheConfig}即可
 *
 * @author zhouhao
 * @see org.springframework.cache.annotation.CacheConfig
 * @see Cacheable
 * @see CacheEvict
 * @since 3.0
 */
public abstract class EnableCacheGenericEntityService<E extends GenericEntity<PK>, PK> extends GenericEntityService<E, PK> {

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
                    @CacheEvict(key = "'id:'+#entity.id"),
                    @CacheEvict(key = "'all'"),
                    @CacheEvict(key = "'count'")
            }
    )
    protected int updateByPk(E entity) {
        return super.updateByPk(entity);
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
    public int deleteByPk(PK pk) {
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
