package org.hswebframework.web.service;

import org.hswebframework.web.commons.entity.TreeSortSupportEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.Collection;
import java.util.List;

/**
 * @author zhouhao
 * @since 3.0
 */
public abstract class EnableCacheAllEvictTreeSortService<E extends TreeSortSupportEntity<PK>, PK>
        extends AbstractTreeSortService<E, PK> {

    @Override
    @Cacheable(key = "'parent:'+#parentId")
    public List<E> selectParentNode(PK parentId) {
        return super.selectParentNode(parentId);
    }

    @Override
    @Cacheable(key = "'chidlren:'+#parentId")
    public List<E> selectChildNode(PK parentId) {
        return super.selectChildNode(parentId);
    }

    @Override
    @Cacheable(key = "'all-chidlren:'+#parentId")
    public List<E> selectAllChildNode(PK parentId) {
        return super.selectAllChildNode(parentId);
    }

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
    @Cacheable(key = "'id-in:'+#id.hashCode()")
    public List<E> selectByPk(List<PK> id) {
        return super.selectByPk(id);
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
