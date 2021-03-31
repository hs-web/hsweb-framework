package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.utils.RandomUtil;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.hswebframework.web.api.crud.entity.TreeSupportEntity;
import org.hswebframework.web.id.IDGenerator;
import org.reactivestreams.Publisher;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @param <E> TreeSortSupportEntity
 * @param <K> ID
 * @see GenericReactiveTreeSupportCrudService
 */
public interface ReactiveTreeSortEntityService<E extends TreeSortSupportEntity<K>, K>
        extends ReactiveCrudService<E, K> {

    default Mono<List<E>> queryResultToTree(Mono<? extends QueryParamEntity> paramEntity) {
        return paramEntity.flatMap(this::queryResultToTree);
    }

    default Mono<List<E>> queryResultToTree(QueryParamEntity paramEntity) {
        return query(paramEntity)
                .collectList()
                .map(list -> TreeSupportEntity.list2tree(list,
                                                         this::setChildren,
                                                         this::createRootNodePredicate));
    }

    default Mono<List<E>> queryIncludeChildrenTree(QueryParamEntity paramEntity) {
        return queryIncludeChildren(paramEntity)
                .collectList()
                .map(list -> TreeSupportEntity.list2tree(list,
                                                         this::setChildren,
                                                         this::createRootNodePredicate));
    }

    default Flux<E> queryIncludeChildren(Collection<K> idList) {
        return findById(idList)
                .flatMap(e -> createQuery()
                        .where()
                        .like$("path", e.getPath())
                        .fetch());
    }

    default Flux<E> queryIncludeChildren(QueryParamEntity queryParam) {
        return query(queryParam)
                .flatMap(e -> createQuery()
                        .where()
                        .like$("path", e.getPath())
                        .fetch());
    }

    @Override
    default Mono<Integer> insert(Publisher<E> entityPublisher) {
        return insertBatch(Flux.from(entityPublisher).collectList());
    }

    @Override
    default Mono<Integer> insertBatch(Publisher<? extends Collection<E>> entityPublisher) {
        return this.getRepository()
                   .insertBatch(Flux.from(entityPublisher)
                                    .flatMap(Flux::fromIterable)
                                    .flatMap(this::applyTreeProperty)
                                    .flatMap(e -> Flux.fromIterable(TreeSupportEntity.expandTree2List(e, getIDGenerator())))
                                    .collectList());
    }

    default Mono<E> applyTreeProperty(E ele) {
        if (StringUtils.hasText(ele.getPath()) ||
                StringUtils.isEmpty(ele.getParentId())) {
            return Mono.just(ele);
        }

        return this.checkCyclicDependency(ele.getId(), ele)
                   .then(this.findById(ele.getParentId())
                             .doOnNext(parent -> ele.setPath(parent.getPath() + "-" + RandomUtil.randomChar(4))))
                   .thenReturn(ele);
    }

    //校验是否有循环依赖,修改父节点为自己的子节点?
    default Mono<E> checkCyclicDependency(K id, E ele) {
        if (StringUtils.isEmpty(id)) {
            return Mono.empty();
        }
        return this
                .queryIncludeChildren(Collections.singletonList(id))
                .doOnNext(e -> {
                    if (Objects.equals(ele.getParentId(), e.getId())) {
                        throw new IllegalArgumentException("不能修改父节点为自己或者自己的子节点");
                    }
                })
                .then(Mono.just(ele));
    }

    @Override
    default Mono<SaveResult> save(Publisher<E> entityPublisher) {
        return this.getRepository()
                   .save(Flux.from(entityPublisher)
                             .flatMap(this::applyTreeProperty)
                             //把树结构平铺
                             .flatMap(e -> Flux.fromIterable(TreeSupportEntity.expandTree2List(e, getIDGenerator())))
                   );
    }

    @Override
    default Mono<Integer> updateById(K id, Mono<E> entityPublisher) {
        return this
                .save(entityPublisher.doOnNext(e -> e.setId(id)))
                .map(SaveResult::getTotal);
    }

    @Override
    default Mono<Integer> deleteById(Publisher<K> idPublisher) {
        return findById(Flux.from(idPublisher))
                .flatMap(e -> createDelete()
                        .where()
                        .like$(e::getPath)
                        .execute())
                .collect(Collectors.summingInt(Integer::intValue));
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
            if (!StringUtils.isEmpty(node.getParentId())) {
                return helper.getNode(node.getParentId()) == null;
            }
            return false;
        };
    }

    default boolean isRootNode(E entity) {
        return StringUtils.isEmpty(entity.getParentId()) || "-1".equals(String.valueOf(entity.getParentId()));
    }
}
