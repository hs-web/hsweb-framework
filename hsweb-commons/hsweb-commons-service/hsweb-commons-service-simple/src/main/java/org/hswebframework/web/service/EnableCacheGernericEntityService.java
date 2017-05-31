package org.hswebframework.web.service;

import org.hswebframework.web.commons.entity.GenericEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public abstract class EnableCacheGernericEntityService<E extends GenericEntity<PK>, PK> extends GenericEntityService<E, PK> {

    @Override
    @Cacheable(key = "'ids:'+#id.hashCode()", condition = "#id!=null")
    public List<E> selectByPk(List<PK> id) {
        return super.selectByPk(id);
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
    @CacheEvict(allEntries = true)
    public int updateByPk(PK pk, E entity) {
        return super.updateByPk(pk, entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    protected int updateByPk(E entity) {
        return super.updateByPk(entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    public PK insert(E entity) {
        return super.insert(entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    public int deleteByPk(PK pk) {
        return super.deleteByPk(pk);
    }

    @Override
    @CacheEvict(allEntries = true)
    public PK saveOrUpdate(E entity) {
        return super.saveOrUpdate(entity);
    }

}
