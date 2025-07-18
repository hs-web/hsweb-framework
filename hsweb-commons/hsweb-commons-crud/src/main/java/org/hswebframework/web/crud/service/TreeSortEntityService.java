package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.utils.RandomUtil;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.hswebframework.web.api.crud.entity.TreeSupportEntity;
import org.hswebframework.web.id.IDGenerator;
import org.reactivestreams.Publisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @param <E> TreeSortSupportEntity
 * @param <K> ID
 * @see GenericReactiveTreeSupportCrudService
 */
public interface TreeSortEntityService<E extends TreeSortSupportEntity<K>, K>
    extends CrudService<E, K> {

    @Transactional(readOnly = true, transactionManager = TransactionManagers.jdbcTransactionManager)
    default List<E> queryResultToTree(QueryParamEntity paramEntity) {
        return TreeSupportEntity
            .list2tree(query(paramEntity),
                       this::setChildren,
                       this::createRootNodePredicate);
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.jdbcTransactionManager)
    default List<E> queryIncludeChildrenTree(QueryParamEntity paramEntity) {

        return TreeSupportEntity
            .list2tree(queryIncludeChildren(paramEntity),
                       this::setChildren,
                       this::createRootNodePredicate);
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.jdbcTransactionManager)
    default List<E> queryIncludeChildren(Collection<K> idList) {
        return findById(idList)
            .stream()
            .flatMap(e -> createQuery()
                .where()
                .like$("path", e.getPath())
                .fetch()
                .stream())
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true, transactionManager = TransactionManagers.jdbcTransactionManager)
    default List<E> queryIncludeChildren(QueryParamEntity queryParam) {
        return query(queryParam)
            .stream()
            .flatMap(e -> createQuery()
                .where()
                .like$("path", e.getPath())
                .fetch()
                .stream())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.jdbcTransactionManager)
    default void insert(E entityPublisher) {
        insert(Collections.singletonList(entityPublisher));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.jdbcTransactionManager)
    default int insert(Collection<E> entityPublisher) {
        return new SyncTreeSortServiceHelper<>(this)
            .prepare(Flux.fromIterable(entityPublisher))
            .buffer(getBufferSize())
            .map(this.getRepository()::insertBatch)
            .reduce(Math::addExact)
            .blockOptional()
            .orElse(0);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.jdbcTransactionManager)
    default SaveResult save(List<E> entities) {
        return new SyncTreeSortServiceHelper<>(this)
            .prepare(Flux.fromIterable(entities))
            .buffer(getBufferSize())
            .map(this.getRepository()::save)
            .reduce(SaveResult::merge)
            .blockOptional()
            .orElse(SaveResult.of(0,0));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.jdbcTransactionManager)
    default int updateById(K id, E entity) {
        entity.setId(id);
        return this.save(entity).getTotal();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.jdbcTransactionManager)
    default int deleteById(Collection<K> idPublisher) {
        List<E> dataList = findById(idPublisher);
        return dataList
            .stream()
            .map(e -> createDelete()
                .where()
                .like$(e::getPath)
                .execute())
            .mapToInt(Integer::intValue)
            .sum();
    }

    IDGenerator<K> getIDGenerator();

    void setChildren(E entity, List<E> children);

    default List<E> getChildren(E entity) {
        return entity.getChildren();
    }

    default int getBufferSize() {
        return 200;
    }

    default Predicate<E> createRootNodePredicate(TreeSupportEntity.TreeHelper<E, K> helper) {
        return node -> {
            if (isRootNode(node)) {
                return true;
            }
            //有父节点,但是父节点不存在
            if (!ObjectUtils.isEmpty(node.getParentId())) {
                return helper.getNode(node.getParentId()) == null;
            }
            return false;
        };
    }

    default boolean isRootNode(E entity) {
        return ObjectUtils.isEmpty(entity.getParentId()) || "-1".equals(String.valueOf(entity.getParentId()));
    }
}
