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
    default void insert(E entityPublisher) {
        insert(Collections.singletonList(entityPublisher));
    }

    @Override
    default int insert(Collection<E> entityPublisher) {
        return this
                .getRepository()
                .insertBatch(entityPublisher
                                     .stream()
                                     .flatMap(this::applyTreeProperty)
                                     .flatMap(e -> TreeSupportEntity
                                             .expandTree2List(e, getIDGenerator())
                                             .stream())
                                     .collect(Collectors.toList())
                );
    }

    default Stream<E> applyTreeProperty(E ele) {
        if (StringUtils.hasText(ele.getPath()) ||
                ObjectUtils.isEmpty(ele.getParentId())) {
            return Stream.of(ele);
        }

        this.checkCyclicDependency(ele.getId(), ele);
        this.findById(ele.getParentId())
            .ifPresent(parent -> ele.setPath(parent.getPath() + "-" + RandomUtil.randomChar(4)));
        return Stream.of(ele);
    }

    //校验是否有循环依赖,修改父节点为自己的子节点?
    default void checkCyclicDependency(K id, E ele) {
        if (ObjectUtils.isEmpty(id)) {
            return;
        }
        for (E e : this.queryIncludeChildren(Collections.singletonList(id))) {
            if (Objects.equals(ele.getParentId(), e.getId())) {
                throw new IllegalArgumentException("不能修改父节点为自己或者自己的子节点");
            }
        }

    }

    @Override
    default SaveResult save(List<E> entities) {
        return this.getRepository()
                   .save(entities
                                 .stream()
                                 .flatMap(this::applyTreeProperty)
                                 //把树结构平铺
                                 .flatMap(e -> TreeSupportEntity
                                         .expandTree2List(e, getIDGenerator())
                                         .stream())
                                 .collect(Collectors.toList())
                   );
    }

    @Override
    default int updateById(K id, E entity) {
        entity.setId(id);
        return this.save(entity).getTotal();
    }

    @Override
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
