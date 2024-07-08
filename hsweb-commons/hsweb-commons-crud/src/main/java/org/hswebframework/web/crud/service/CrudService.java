package org.hswebframework.web.crud.service;

import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.rdb.mapping.SyncDelete;
import org.hswebframework.ezorm.rdb.mapping.SyncQuery;
import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.ezorm.rdb.mapping.SyncUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
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

    @Transactional(readOnly = true, transactionManager = TransactionManagers.jdbcTransactionManager)
    default Optional<E> findById(K id) {
        return getRepository()
                .findById(id);
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.jdbcTransactionManager)
    default List<E> findById(Collection<K> id) {
        if (CollectionUtils.isEmpty(id)) {
            return Collections.emptyList();
        }
        return this
                .getRepository()
                .findById(id);
    }

    @Transactional(transactionManager = TransactionManagers.jdbcTransactionManager)
    default SaveResult save(Collection<E> entityArr) {
        return getRepository()
                .save(entityArr);
    }

    @Transactional(transactionManager = TransactionManagers.jdbcTransactionManager)
    default int insert(Collection<E> entityArr) {
        return getRepository()
                .insertBatch(entityArr);
    }

    @Transactional(transactionManager = TransactionManagers.jdbcTransactionManager)
    default void insert(E entityArr) {
        getRepository()
                .insert(entityArr);
    }

    @Transactional(transactionManager = TransactionManagers.jdbcTransactionManager)
    default int updateById(K id, E entityArr) {
        return getRepository()
                .updateById(id, entityArr);
    }

    @Transactional(transactionManager = TransactionManagers.jdbcTransactionManager)
    default SaveResult save(E entity) {
        return getRepository()
                .save(Collections.singletonList(entity));
    }

    @Transactional(transactionManager = TransactionManagers.jdbcTransactionManager)
    default SaveResult save(List<E> entities) {
        return getRepository()
                .save(entities);
    }

    @Transactional(transactionManager = TransactionManagers.jdbcTransactionManager)
    default int deleteById(Collection<K> idArr) {
        return getRepository().deleteById(idArr);
    }

    @Transactional(transactionManager = TransactionManagers.jdbcTransactionManager)
    default int deleteById(K idArr) {
        return deleteById(Collections.singletonList(idArr));
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.jdbcTransactionManager)
    default List<E> query(QueryParamEntity queryParam) {
        return createQuery().setParam(queryParam).fetch();
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.jdbcTransactionManager)
    default PagerResult<E> queryPager(QueryParamEntity param) {

        int count = param.getTotal() == null ? count(param) : param.getTotal();
        if (count == 0) {
            return PagerResult.empty();
        }
        param.rePaging(count);

        return PagerResult.of(count, query(param), param);
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.jdbcTransactionManager)
    default int count(QueryParam param) {
        return getRepository()
                .createQuery()
                .setParam(param)
                .count();
    }

}
