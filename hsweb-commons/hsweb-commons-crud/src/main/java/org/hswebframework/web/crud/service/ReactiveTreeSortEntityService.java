package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.ezorm.rdb.operator.dml.Terms;
import org.hswebframework.utils.RandomUtil;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.hswebframework.web.api.crud.entity.TreeSupportEntity;
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
                        .fetch())
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
                        .fetch())
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
                        .where()
                        .like$("path", e.getPath())
                        .fetch()
                )
                .distinct(TreeSupportEntity::getId);
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> insert(Publisher<E> entityPublisher) {
        return insertBatch(Flux.from(entityPublisher).collectList());
    }

    @Override
    default Mono<Integer> insertBatch(Publisher<? extends Collection<E>> entityPublisher) {
        return this.getRepository()
                   .insertBatch(Flux.from(entityPublisher)
                                    .flatMap(this::checkParentId)
                                    .flatMap(Flux::fromIterable)
                                    .flatMap(this::applyTreeProperty)
                                    .flatMap(e -> Flux.fromIterable(TreeSupportEntity.expandTree2List(e, getIDGenerator())))
                                    .collectList());
    }

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

    default Mono<Collection<E>> checkParentId(Collection<E> source) {
        
        Set<K> idSet = source
                .stream()
                .map(TreeSupportEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (idSet.isEmpty()) {
            return Mono.just(source);
        }

        Set<K> readyToCheck = source
                .stream()
                .map(TreeSupportEntity::getParentId)
                .filter(e -> Objects.nonNull(e) && !idSet.contains(e))
                .collect(Collectors.toSet());

        if (readyToCheck.isEmpty()) {
            return Mono.just(source);
        }

        return this
                .createQuery()
                .in("id", readyToCheck)
                .count()
                .doOnNext(count -> {
                    if (count != readyToCheck.size()) {
                        throw new ValidationException("parentId", "error.tree_entity_parent_id_not_exist");
                    }
                })
                .thenReturn(source);

    }

    //重构子节点的path
    default Mono<Void> refactorChildPath(K id, String path, Consumer<E> pathAccepter) {
        return this
                .createQuery()
                .where("parentId", id)
                .fetch()
                .flatMap(e -> {
                    if (ObjectUtils.isEmpty(path)) {
                        e.setPath(RandomUtil.randomChar(4));
                    } else {
                        e.setPath(path + "-" + RandomUtil.randomChar(4));
                    }
                    pathAccepter.accept(e);
                    if (e.getParentId() != null) {
                        return this
                                .refactorChildPath(e.getId(), e.getPath(), pathAccepter)
                                .thenReturn(e);
                    }
                    return Mono.just(e);
                })
                .as(getRepository()::save)
                .then();
    }

    @Override
    default Mono<SaveResult> save(Publisher<E> entityPublisher) {
        return Flux
                .from(entityPublisher)
                //1.先平铺
                .flatMapIterable(e -> TreeSupportEntity.expandTree2List(e, getIDGenerator()))
                .collectList()
                .flatMap(this::checkParentId)
                .flatMapIterable(list -> {
                    Map<K, E> map = list
                            .stream()
                            .filter(e -> e.getId() != null)
                            .collect(Collectors.toMap(TreeSupportEntity::getId, Function.identity()));
                    //2. 重新组装树结构
                    return TreeSupportEntity.list2tree(list,
                                                       this::setChildren,
                                                       (Predicate<E>) e -> this.isRootNode(e) || map.get(e.getParentId()) == null);

                })
                //执行验证
                .doOnNext(e -> e.tryValidate(CreateGroup.class))
                //再次平铺为
                .flatMapIterable(e -> TreeSupportEntity.expandTree2List(e, getIDGenerator()))
                //重构path
                .as(this::tryRefactorPath)
                .as(this.getRepository()::save);

    }

    default Flux<E> tryRefactorPath(Flux<E> stream) {
        Flux<E> cache = stream.cache();
        Mono<Map<K, E>> mapping = cache
                .filter(e -> null != e.getId())
                .collectMap(TreeSupportEntity::getId, Function.identity())
                .defaultIfEmpty(Collections.emptyMap());

        //查询出旧数据
        Mono<Map<K, E>> olds = cache
                .filter(e -> null != e.getId())
                .map(TreeSupportEntity::getId)
                .as(this::findById)
                .collectMap(TreeSupportEntity::getId, Function.identity())
                .defaultIfEmpty(Collections.emptyMap());


        return Mono
                .zip(mapping, olds)
                .flatMapMany(tp2 -> {
                    Map<K, E> map = tp2.getT1();
                    Map<K, E> oldMap = tp2.getT2();

                    return cache
                            .flatMap(data -> {
                                E old = data.getId() == null ? null : oldMap.get(data.getId());
                                K parentId = old != null ? old.getParentId() : data.getParentId();
                                E oldParent = parentId == null ? null : oldMap.get(parentId);
                                if (old != null) {
                                    K newParentId = data.getParentId();
                                    //父节点发生变化，更新所有子节点path
                                    if (!Objects.equals(parentId, newParentId)) {
                                        List<Mono<Void>> jobs = new ArrayList<>();
                                        Consumer<E> childConsumer = child -> {
                                            //更新了父节点,但是同时也传入的对应的子节点
                                            E readyToUpdate = map.get(child.getId());
                                            if (null != readyToUpdate) {
                                                readyToUpdate.setPath(child.getPath());
                                            }
                                        };

                                        //变更到了顶级节点
                                        if (isRootNode(data)) {
                                            data.setPath(RandomUtil.randomChar(4));
                                            jobs.add(this.refactorChildPath(old.getId(), data.getPath(), childConsumer));
                                        } else {
                                            if (null != oldParent) {
                                                data.setPath(oldParent.getPath() + "-" + RandomUtil.randomChar(4));
                                                jobs.add(this.refactorChildPath(old.getId(), data.getPath(), childConsumer));
                                            } else {
                                                jobs.add(this.findById(newParentId)
                                                             .flatMap(parent -> {
                                                                 data.setPath(parent.getPath() + "-" + RandomUtil.randomChar(4));
                                                                 return this.refactorChildPath(data.getId(), data.getPath(), childConsumer);
                                                             })
                                                );
                                            }
                                        }
                                        return Flux.merge(jobs)
                                                   .then(Mono.just(data));
                                    } else {
                                        //父节点未变化则使用原始的path
                                        Consumer<E> pathRefactor = (parent) -> {
                                            if (old.getPath().startsWith(parent.getPath())) {
                                                data.setPath(old.getPath());
                                            } else {
                                                data.setPath(parent.getPath() + "-" + RandomUtil.randomChar(4));
                                            }
                                        };
                                        if (oldParent != null) {
                                            pathRefactor.accept(oldParent);
                                        } else if (parentId != null) {
                                            return findById(parentId)
                                                    .switchIfEmpty(Mono.fromRunnable(() -> {
                                                        data.setParentId(null);
                                                        data.setLevel(1);
                                                        data.setPath(old.getPath());
                                                    }))
                                                    .doOnNext(pathRefactor)
                                                    .thenReturn(data);
                                        } else {
                                            data.setPath(old.getPath());
                                        }

                                    }
                                }
                                return Mono.just(data);
                            });
                });
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
                        ? createDelete().where().like$(e::getPath).execute()
                        : getRepository().deleteById(e.getId()))
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
}
