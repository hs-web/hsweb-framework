package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.ezorm.rdb.operator.dml.Terms;
import org.hswebframework.utils.RandomUtil;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.hswebframework.web.api.crud.entity.TreeSupportEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.validator.CreateGroup;
import org.reactivestreams.Publisher;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Consumer;
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
        Set<String> duplicateCheck = new HashSet<>();

        return findById(idList)
                .concatMap(e -> StringUtils
                        .isEmpty(e.getPath())|| !duplicateCheck.add(e.getPath())
                        ? Mono.just(e)
                        : createQuery()
                        .where()
                        .like$("path", e.getPath())
                        .fetch())
                .distinct(TreeSupportEntity::getId);
    }

    default Flux<E> queryIncludeParent(Collection<K> idList) {
        Set<String> duplicateCheck = new HashSet<>();

        return findById(idList)
                .concatMap(e -> StringUtils
                        .isEmpty(e.getPath())|| !duplicateCheck.add(e.getPath())
                        ? Mono.just(e)
                        : createQuery()
                        .where()
                        .accept(Terms.Like.reversal("path", e.getPath(), false, true))
                        .notEmpty("path")
                        .notNull("path")
                        .fetch())
                .distinct(TreeSupportEntity::getId);
    }

    default Flux<E> queryIncludeChildren(QueryParamEntity queryParam) {
        Set<String> duplicateCheck = new HashSet<>();

        return query(queryParam)
                .concatMap(e -> StringUtils
                        .isEmpty(e.getPath()) || !duplicateCheck.add(e.getPath())
                        ? Mono.just(e)
                        : createQuery()
                        .where()
                        .like$("path", e.getPath())
                        .fetch()
                )
                .distinct(TreeSupportEntity::getId);
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

    default Mono<Void> refactorChildPath(K id, String path, Consumer<E> pathAccepter) {
        return this
                .createQuery()
                .where("parentId", id)
                .fetch()
                .flatMap(e -> {
                    if (StringUtils.isEmpty(path)) {
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
                .flatMapIterable(e -> TreeSupportEntity.expandTree2List(e, getIDGenerator()))
                .collectList()
                .flatMapIterable(list -> {
                    Map<K, E> map = list
                            .stream()
                            .filter(e -> e.getId() != null)
                            .collect(Collectors.toMap(TreeSupportEntity::getId, Function.identity()));

                    return TreeSupportEntity.list2tree(list,
                                                       this::setChildren,
                                                       (Predicate<E>) e -> this.isRootNode(e) || map.get(e.getParentId()) == null);

                })
                .doOnNext(e -> e.tryValidate(CreateGroup.class))
                .flatMapIterable(e -> TreeSupportEntity.expandTree2List(e, getIDGenerator()))
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
    default Mono<SaveResult> save(Collection<E> collection) {
        return save(Flux.fromIterable(collection));
    }

    @Override
    default Mono<SaveResult> save(E data) {
        return save(Flux.just(data));
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
