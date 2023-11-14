package org.hswebframework.web.crud.service;

import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.ezorm.rdb.operator.dml.Terms;
import org.hswebframework.utils.RandomUtil;
import org.hswebframework.web.api.crud.entity.*;
import org.hswebframework.web.exception.ValidationException;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.validator.CreateGroup;
import org.reactivestreams.Publisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.math.MathFlux;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 树形结构的通用增删改查服务
 *
 * @param <E> TreeSortSupportEntity
 * @param <K> ID
 * @see GenericReactiveTreeSupportCrudService
 */
public interface ReactiveTreeSortEntityService<E extends TreeSortSupportEntity<K>, K>
        extends ReactiveCrudService<E, K> {

    /**
     * 动态查询并将查询结构转为树形结构
     *
     * @param paramEntity 查询参数
     * @return 树形结构
     */
    default Mono<List<E>> queryResultToTree(Mono<? extends QueryParamEntity> paramEntity) {
        return paramEntity.flatMap(this::queryResultToTree);
    }

    /**
     * 动态查询并将查询结构转为树形结构
     *
     * @param paramEntity 查询参数
     * @return 树形结构
     */
    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<List<E>> queryResultToTree(QueryParamEntity paramEntity) {
        return query(paramEntity)
                .collectList()
                .map(list -> TreeSupportEntity.list2tree(list,
                                                         this::setChildren,
                                                         this::createRootNodePredicate));
    }

    /**
     * 动态查询并将查询结构转为树形结构,包含所有子节点
     *
     * @param paramEntity 查询参数
     * @return 树形结构
     */
    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<List<E>> queryIncludeChildrenTree(QueryParamEntity paramEntity) {
        return queryIncludeChildren(paramEntity)
                .collectList()
                .map(list -> TreeSupportEntity.list2tree(list,
                                                         this::setChildren,
                                                         this::createRootNodePredicate));
    }

    /**
     * 查询指定ID的实体以及对应的全部子节点
     *
     * @param idList ID集合
     * @return 树形结构
     */
    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> queryIncludeChildren(Collection<K> idList) {
        Set<String> duplicateCheck = new HashSet<>();

        return findById(idList)
                .concatMap(e -> !StringUtils.hasText(e.getPath()) || !duplicateCheck.add(e.getPath())
                                   ? Mono.just(e)
                                   : createQuery()
                                   .where()
                                   //使用path快速查询
                                   .like$("path", e.getPath())
                                   .fetch(),
                           Integer.MAX_VALUE)
                .distinct(TreeSupportEntity::getId);
    }

    /**
     * 查询指定ID的实体以及对应的全部父节点
     *
     * @param idList ID集合
     * @return 树形结构
     */
    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> queryIncludeParent(Collection<K> idList) {
        Set<String> duplicateCheck = new HashSet<>();

        return findById(idList)
                .concatMap(e -> !StringUtils.hasText(e.getPath()) || !duplicateCheck.add(e.getPath())
                        ? Mono.just(e)
                        : createQuery()
                        .where()
                        //where ? like path and path !='' and path not null
                        .accept(Terms.Like.reversal("path", e.getPath(), false, true))
                        .notEmpty("path")
                        .notNull("path")
                        .fetch(), Integer.MAX_VALUE)
                .distinct(TreeSupportEntity::getId);
    }

    /**
     * 动态查询并将查询结构转为树形结构
     *
     * @param queryParam 查询参数
     * @return 树形结构
     */
    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> queryIncludeChildren(QueryParamEntity queryParam) {
        Set<String> duplicateCheck = new HashSet<>();

        return query(queryParam)
                .concatMap(e -> !StringUtils.hasText(e.getPath()) || !duplicateCheck.add(e.getPath())
                        ? Mono.just(e)
                        : createQuery()
                        .as(q -> {
                            if (CollectionUtils.isNotEmpty(queryParam.getIncludes())) {
                                q.select(queryParam.getIncludes().toArray(new String[0]));
                            }
                            if (CollectionUtils.isNotEmpty(queryParam.getExcludes())) {
                                q.selectExcludes(queryParam.getExcludes().toArray(new String[0]));
                            }
                            return q;
                        })
                        .where()
                        .like$("path", e.getPath())
                        .fetch()
                ,Integer.MAX_VALUE)
                .distinct(TreeSupportEntity::getId);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> insert(Publisher<E> entityPublisher) {
        return insertBatch(Flux.from(entityPublisher).collectList());
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> insert(E data) {
        return this.insertBatch(Flux.just(Collections.singletonList(data)));
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> insertBatch(Publisher<? extends Collection<E>> entityPublisher) {
        return this
                .getRepository()
                .insertBatch(new TreeSortServiceHelper<>(this)
                                     .prepare(Flux.from(entityPublisher)
                                                  .flatMapIterable(Function.identity()))
                                     //  .doOnNext(e -> e.tryValidate(CreateGroup.class))
                                     .buffer(getBufferSize()));
    }

    default int getBufferSize() {
        return 200;
    }

    @Deprecated
    default Mono<E> applyTreeProperty(E ele) {
        if (StringUtils.hasText(ele.getPath()) ||
                ObjectUtils.isEmpty(ele.getParentId())) {
            return Mono.just(ele);
        }

        return this.checkCyclicDependency(ele.getId(), ele)
                   .then(this.findById(ele.getParentId())
                             .doOnNext(parent -> ele.setPath(parent.getPath() + "-" + RandomUtil.randomChar(4))))
                   .thenReturn(ele);
    }

    @Deprecated
    //校验是否有循环依赖,修改父节点为自己的子节点?
    default Mono<E> checkCyclicDependency(K id, E ele) {
        if (ObjectUtils.isEmpty(id)) {
            return Mono.empty();
        }
        return this
                .queryIncludeChildren(Collections.singletonList(id))
                .doOnNext(e -> {
                    if (Objects.equals(ele.getParentId(), e.getId())) {
                        throw new ValidationException("parentId", "error.tree_entity_cyclic_dependency");
                    }
                })
                .then(Mono.just(ele));
    }

    @Deprecated
    default Mono<Collection<E>> checkParentId(Collection<E> source) {

        Set<K> idSet = source
                .stream()
                .map(TreeSupportEntity::getId)
                .filter(e -> !ObjectUtils.isEmpty(e))
                .collect(Collectors.toSet());

        if (idSet.isEmpty()) {
            return Mono.just(source);
        }

        Set<K> readyToCheck = source
                .stream()
                .map(TreeSupportEntity::getParentId)
                .filter(e -> !ObjectUtils.isEmpty(e) && !idSet.contains(e))
                .collect(Collectors.toSet());

        if (readyToCheck.isEmpty()) {
            return Mono.just(source);
        }

        return this
                .createQuery()
                .select("id")
                .in("id", readyToCheck)
                .fetch()
                .doOnNext(e -> readyToCheck.remove(e.getId()))
                .then(Mono.fromSupplier(() -> {
                    if (!readyToCheck.isEmpty()) {
                        throw new ValidationException(
                                "error.tree_entity_parent_id_not_exist",
                                Collections.singletonList(
                                        new ValidationException.Detail(
                                                "parentId",
                                                "error.tree_entity_parent_id_not_exist",
                                                readyToCheck))
                        );
                    }
                    return source;
                }));

    }

    @Deprecated
    //重构子节点的path
    default void refactorChildPath(K id, Function<K, Collection<E>> childGetter, String path, Consumer<E> pathAccepter) {

        Collection<E> children = childGetter.apply(id);
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        for (E child : children) {
            if (ObjectUtils.isEmpty(path)) {
                child.setPath(RandomUtil.randomChar(4));
            } else {
                child.setPath(path + "-" + RandomUtil.randomChar(4));
            }
            pathAccepter.accept(child);
            this.refactorChildPath(child.getId(), childGetter, child.getPath(), pathAccepter);
        }

    }

    @Override
    @Transactional(rollbackFor = Throwable.class,
            transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<SaveResult> save(Publisher<E> entityPublisher) {
        return new TreeSortServiceHelper<>(this)
                .prepare(Flux.from(entityPublisher))
//                .doOnNext(e -> e.tryValidate(CreateGroup.class))
                .buffer(getBufferSize())
                .flatMap(this.getRepository()::save)
                .reduce(SaveResult::merge);

    }

    @Deprecated
    default Flux<E> tryRefactorPath(Flux<E> stream) {
        return new TreeSortServiceHelper<>(this).prepare(stream);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<SaveResult> save(Collection<E> collection) {
        return save(Flux.fromIterable(collection));
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<SaveResult> save(E data) {
        return save(Flux.just(data));
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> updateById(K id, Mono<E> entityPublisher) {
        return this
                .findById(id)
                .map(e -> this
                        .save(entityPublisher.doOnNext(data -> data.setId(id)))
                        .map(SaveResult::getTotal))
                .defaultIfEmpty(Mono.just(0))
                .flatMap(Function.identity());
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> deleteById(Publisher<K> idPublisher) {
        return this
                .findById(Flux.from(idPublisher))
                .concatMap(e -> StringUtils.hasText(e.getPath())
                        ? getRepository().createDelete().where().like$(e::getPath).execute()
                        : getRepository().deleteById(e.getId()),Integer.MAX_VALUE)
                .as(MathFlux::sumInt);
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

    @Override
    @SuppressWarnings("all")
    default ReactiveDelete createDelete() {
        return ReactiveCrudService.super
                .createDelete()
                .onExecute((delete, executor) -> this
                        .queryIncludeChildren(delete.toQueryParam(QueryParamEntity::new)
                                                    .<QueryParamEntity>includes("id", "path", "parentId"))
                        .map(TreeSupportEntity::getId)
                        .buffer(200)
                        .concatMap(list -> getRepository()
                                .createDelete()
                                .where()
                                .in("id", list)
                                .execute(), Integer.MAX_VALUE)
                        //.concatWith(executor)
                        .reduce(0, Math::addExact));
    }
}
