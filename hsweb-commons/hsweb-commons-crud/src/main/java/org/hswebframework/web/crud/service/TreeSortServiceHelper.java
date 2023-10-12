package org.hswebframework.web.crud.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.hswebframework.utils.RandomUtil;
import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.hswebframework.web.api.crud.entity.TreeSupportEntity;
import org.hswebframework.web.exception.ValidationException;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TreeSortServiceHelper<E extends TreeSortSupportEntity<PK>, PK> {

    //包含子节点的数据
    private Map<PK, E> allData;

    private Map<PK, E> oldData;

    private Map<PK, E> thisTime;

    private Map<PK, E> readyToSave;

    private final Map<PK, Map<PK, E>> childrenMapping = new LinkedHashMap<>();

    private final ReactiveTreeSortEntityService<E, PK> service;

    TreeSortServiceHelper(ReactiveTreeSortEntityService<E, PK> service) {
        this.service = service;
    }

    Flux<E> prepare(Flux<E> source) {
        Flux<E> cache = source
                .flatMapIterable(e -> TreeSupportEntity.expandTree2List(e, service.getIDGenerator()))
                .collectList()
                .flatMapIterable(list -> {

                    Map<PK, E> map = list
                            .stream()
                            .filter(e -> e.getId() != null)
                            .collect(Collectors.toMap(
                                    TreeSupportEntity::getId,
                                    Function.identity(),
                                    (a, b) -> a
                            ));
                    //重新组装树结构
                    TreeSupportEntity.list2tree(list,
                                                service::setChildren,
                                                (Predicate<E>) e -> service.isRootNode(e) || map.get(e.getParentId()) == null);

                    return list;
                })
                .cache();

        return init(cache)
                .then(Mono.defer(this::checkParentId))
                .then(Mono.fromRunnable(this::checkCyclicDependency))
                .then(Mono.fromRunnable(this::refactorPath))
                .thenMany(Flux.defer(() -> Flux.fromIterable(readyToSave.values())))
                .doOnNext(this::refactor);
    }

    private Mono<Void> init(Flux<E> source) {
        oldData = new LinkedHashMap<>();
        thisTime = new LinkedHashMap<>();
        allData = new LinkedHashMap<>();
        readyToSave = new LinkedHashMap<>();

        Mono<Map<PK, E>> allDataFetcher =
                source
                        .mapNotNull(e -> {

                            if (e.getId() != null) {
                                thisTime.put(e.getId(), e);
                            }

                            return e.getId();
                        })
                        .collect(Collectors.toSet())
                        .flatMap(list -> service
                                .queryIncludeChildren(list)
                                .collectMap(TreeSupportEntity::getId, Function.identity()));
        return allDataFetcher
                .doOnNext(includeChildren -> {
                    //旧的数据
                    for (E value : thisTime.values()) {
                        E old = includeChildren.get(value.getId());
                        if (null != old) {
                            this.oldData.put(value.getId(), old);
                        }
                    }

                    readyToSave.putAll(thisTime);

                    allData.putAll(includeChildren);
                    allData.putAll(this.thisTime);
                    initChildren();

                })
                .then();
    }

    private void initChildren() {
        childrenMapping.clear();

        for (E value : allData.values()) {
            if (service.isRootNode(value) || value.getId() == null) {
                continue;
            }
            childrenMapping
                    .computeIfAbsent(value.getParentId(), ignore -> new LinkedHashMap<>())
                    .put(value.getId(), value);
        }
    }

    private void checkCyclicDependency() {
        for (E value : readyToSave.values()) {
            checkCyclicDependency(value, new LinkedHashSet<>());
        }
    }

    private void checkCyclicDependency(E val, Set<PK> container) {
        if (!container.add(val.getId())) {
            throw new ValidationException("parentId", "error.tree_entity_cyclic_dependency");
        }
        Map<PK, E> children = childrenMapping.get(val.getId());
        if (MapUtils.isNotEmpty(children)) {
            for (Map.Entry<PK, E> entry : children.entrySet()) {
                checkCyclicDependency(entry.getValue(), container);
            }
        }
    }

    private Mono<Void> checkParentId() {

        if (allData.isEmpty()) {
            return Mono.empty();
        }

        Set<PK> readyToCheck = thisTime
                .values()
                .stream()
                .map(TreeSupportEntity::getParentId)
                .filter(e -> !ObjectUtils.isEmpty(e) && !allData.containsKey(e))
                .collect(Collectors.toSet());

        if (readyToCheck.isEmpty()) {
            return Mono.empty();
        }
        return service
                .createQuery()
                .in("id", readyToCheck)
                .fetch()
                .doOnNext(e -> {
                    allData.put(e.getId(), e);
                    readyToCheck.remove(e.getId());
                })
                .then(Mono.fromRunnable(() -> {
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
                    initChildren();
                }));
    }

    private void refactorPath() {
        Function<PK, Collection<E>> childGetter
                = id -> childrenMapping
                .getOrDefault(id, Collections.emptyMap())
                .values();

        for (E data : thisTime.values()) {
            E old = data.getId() == null ? null : oldData.get(data.getId());
            PK parentId = old != null ? old.getParentId() : data.getParentId();
            E oldParent = parentId == null ? null : allData.get(parentId);
            //编辑节点
            if (old != null) {
                PK newParentId = data.getParentId();
                //父节点发生变化，更新所有子节点path
                if (!Objects.equals(parentId, newParentId)) {
                    Consumer<E> childConsumer = child -> {
                        //更新了父节点,但是同时也传入的对应的子节点
                        E readyToUpdate = thisTime.get(child.getId());
                        if (null != readyToUpdate) {
                            readyToUpdate.setPath(child.getPath());
                        }
                    };

                    //变更到了顶级节点
                    if (service.isRootNode(data)) {
                        data.setPath(RandomUtil.randomChar(4));
                        this.refactorChildPath(old.getId(), data.getPath(), childConsumer);
                        //重新保存所有子节点
                        putChildToReadyToSave(childGetter, old);

                    } else {
                        E newParent = allData.get(newParentId);
                        if (null != newParent) {
                            data.setPath(newParent.getPath() + "-" + RandomUtil.randomChar(4));
                            this.refactorChildPath(data.getId(), data.getPath(), childConsumer);
                            //重新保存所有子节点
                            putChildToReadyToSave(childGetter, data);
                        }
                    }
                } else {
                    if (oldParent != null) {
                        if (old.getPath().startsWith(oldParent.getPath())) {
                            data.setPath(old.getPath());
                        } else {
                            data.setPath(oldParent.getPath() + "-" + RandomUtil.randomChar(4));
                        }
                    } else {
                        data.setPath(old.getPath());
                    }
                }
            }

            //新增节点
            else if (parentId != null) {
                if (oldParent != null) {
                    data.setPath(oldParent.getPath() + "-" + RandomUtil.randomChar(4));
                }
            }
        }

    }

    private void putChildToReadyToSave(Function<PK, Collection<E>> childGetter, E data) {
        childGetter
                .apply(data.getId())
                .forEach(e -> {
                    readyToSave.put(e.getId(), e);
                    putChildToReadyToSave(childGetter, e);
                });
    }

    private void refactor(E e) {
        if (e.getPath() != null) {
            e.setLevel(e.getPath().split("-").length);
        }
    }

    //重构子节点的path
    private void refactorChildPath(PK id, String path, Consumer<E> pathAccepter) {

        Collection<E> children = childrenMapping.getOrDefault(id, Collections.emptyMap()).values();
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
            this.refactorChildPath(child.getId(), child.getPath(), pathAccepter);
        }

    }
}
