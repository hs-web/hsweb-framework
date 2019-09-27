package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.rdb.mapping.SyncDelete;
import org.hswebframework.ezorm.rdb.mapping.SyncQuery;
import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.ezorm.rdb.mapping.SyncUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.crud.entity.PagerResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CrudService<E, K> {
    SyncRepository<E, K> getRepository();

    default SyncQuery<E> createQuery() {
        return getRepository().createQuery();
    }

    default SyncUpdate<E> createUpdate() {
        return getRepository().createUpdate();
    }

    default SyncDelete createDelete() {
        return getRepository().createDelete();
    }

    @Transactional(readOnly = true)
    default Optional<E> findById(K id) {
        return getRepository()
                .findById(id);
    }

    @Transactional(readOnly = true)
    default List<E> findById(Collection<K> id) {
        return getRepository()
                .findById(id);
    }

    @Transactional
    default SaveResult save(E... entityArr) {
        return getRepository()
                .save(entityArr);
    }

    @Transactional
    default SaveResult save(Collection<E> entityArr) {
        return getRepository()
                .save(entityArr);
    }

    @Transactional
    default int deleteById(K... idArr) {
        return getRepository().deleteById(idArr);
    }

    @Transactional
    default int deleteById(Collection<K> idArr) {
        return getRepository().deleteById(idArr);
    }

    @Transactional(readOnly = true)
    default List<E> query(QueryParam queryParam) {
        return createQuery().setParam(queryParam).fetch();
    }

    @Transactional(readOnly = true)
    default PagerResult<E> queryPager(QueryParam param) {

        int count = count(param);
        if (count == 0) {
            return PagerResult.empty();
        }
        param.rePaging(count);

        return PagerResult.of(count, query(param), param);
    }

    @Transactional(readOnly = true)
    default Integer count(QueryParam param) {
        return getRepository()
                .createQuery()
                .setParam(param)
                .count();
    }

}
