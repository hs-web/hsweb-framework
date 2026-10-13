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

    @Transactional(transactionManager = TransactionManagers.jdbcTransactionManager)
    default List<E> queryResultToTree(QueryParamEntity paramEntity) {
        return TreeSupportEntity
            .list2tree(query(paramEntity),
                       this::setChildren,
                       this::createRootNodePredicate);
    }

    @Transactional(transactionManager = TransactionManagers.jdbcTransactionManager)
    default List<E> queryIncludeChildrenTree(QueryParamEntity paramEntity) {

        return TreeSupportEntity
            .list2tree(queryIncludeChildren(paramEntity),
                       this::setChildren,
                       this::createRootNodePredicate);
    }

    @Transactional(transactionManager = TransactionManagers.jdbcTransactionManager)
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

    @Transactional(transactionManager = TransactionManagers.jdbcTransactionManager)
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

    /**
     * 按提交实体保存完整树关系，已有节点的 null parentId 表示移根。
     * 遗漏 children 不自动删除后代；业务列的 null 沿用 ORM 保存规则。
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.jdbcTransactionManager)
    default SaveResult save(List<E> entities) {
        SyncTreeSortServiceHelper<E, K> helper = new SyncTreeSortServiceHelper<>(this);
        return helper
            .prepare(Flux.fromIterable(entities))
            .buffer(getBufferSize())
            .map(batch -> {
                for (E node : batch) {
                    if (helper.isMovingToRoot(node)) {
                        // ORM save 忽略普通 null；显式移根与随后保存的路径、实体事件共享原事务。
                        getRepository().createUpdate()
                            .set("path", node.getPath())
                            .set("level", node.getLevel())
                            .setNull("parentId")
                            .where("id", node.getId())
                            .execute();
                    }
                }
                return getRepository().save(batch);
            })
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
