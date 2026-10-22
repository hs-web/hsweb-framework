package org.hswebframework.web.crud.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.hswebframework.utils.RandomUtil;
import org.hswebframework.web.api.crud.entity.EntityFactoryHolder;
import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.hswebframework.web.api.crud.entity.TreeSupportEntity;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.exception.ValidationException;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 为同步及响应式树写入准备父子关系与路径；每次操作创建独立实例。
 * Save 只补齐已有节点未指定的父级，部分 UPDATE 另外维护稀疏业务输入。
 * 根标记委派服务的 isRootNode，准备阶段不执行持久化写入。
 */
public abstract class TreeSortServiceHelper<E extends TreeSortSupportEntity<PK>, PK> {

    //包含子节点的数据
    protected Map<PK, E> allData;

    protected Map<PK, E> oldData;

    protected Map<PK, E> thisTime;

    protected Map<PK, E> readyToSave;

    protected final Map<PK, Map<PK, E>> childrenMapping = new LinkedHashMap<>();

    private final Map<PK, E> updateData = new LinkedHashMap<>();

    private final List<E> updateRoots = new ArrayList<>();

    protected abstract IDGenerator<PK> getIdGenerator();

    protected abstract void applyChildren(E parent, List<E> children);

    protected abstract boolean isRootNode(E node);

    protected abstract Flux<E> queryIncludeChildren(Collection<PK> idList);

    protected abstract Flux<E> queryById(Collection<PK> idList);


    public Flux<E> prepare(Flux<E> source) {
        return prepare(source, false);
    }

    protected Flux<E> prepare(Flux<E> source, boolean partialUpdate) {
        Flux<E> cache = source
            .flatMapIterable(root -> {
                // 展开和持久图补齐只修改工作副本，复用输入或重新订阅仍保留原稀疏意图。
                Map<E, E> workingCopies = partialUpdate ? copyUpdateInput(root) : Collections.emptyMap();
                if (partialUpdate) {
                    root = workingCopies.get(root);
                }
                Map<E, E> patches = partialUpdate ? copyUpdateInput(root) : Collections.emptyMap();
                List<E> expanded = TreeSupportEntity.expandTree2List(root, getIdGenerator());
                if (partialUpdate) {
                    // 沿用新增节点的 ID 回填；其他生成字段不能成为下次调用的显式输入。
                    workingCopies.forEach((input, working) -> input.setId(working.getId()));
                    updateRoots.add(root);
                    for (E node : expanded) {
                        E patch = patches.get(node);
                        patch.setId(node.getId());
                        if (node != root) {
                            // children 的嵌套关系本身就是显式父级意图。
                            patch.setParentId(node.getParentId());
                        }
                        updateData.put(node.getId(), patch);
                    }
                }
                return expanded;
            })
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
                                            this::applyChildren,
                                            (Predicate<E>) e -> isRootNode(e) || map.get(e.getParentId()) == null);

                return list;
            })
            .cache();

        return init(cache, partialUpdate)
            .then(Mono.defer(this::checkParentId))
            .then(Mono.fromRunnable(this::checkCyclicDependency))
            .then(Mono.fromRunnable(this::refactorPath))
            .thenMany(Flux.defer(() -> Flux.fromIterable(readyToSave.values())))
            .doOnNext(this::refactor)
            // sort 稳定保留同层输入顺序；分批前保证父节点位于子节点之前。
            .sort(Comparator.comparing(TreeSupportEntity::getLevel,
                                       Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    @SuppressWarnings("unchecked")
    private Map<E, E> copyUpdateInput(E root) {
        Map<E, E> copies = new IdentityHashMap<>();
        Queue<E> queue = new ArrayDeque<>();
        queue.add(root);
        for (E node = queue.poll(); node != null; node = queue.poll()) {
            if (copies.containsKey(node)) {
                continue;
            }
            Class<E> nodeType = (Class<E>) node.getClass();
            E copy = EntityFactoryHolder.newInstance(
                nodeType,
                () -> BeanUtils.instantiateClass(nodeType));
            FastBeanCopier.copy(node, copy);
            copies.put(node, copy);
            if (CollectionUtils.isNotEmpty(node.getChildren())) {
                queue.addAll(node.<E>getChildren());
            }
        }
        for (Map.Entry<E, E> entry : copies.entrySet()) {
            List<E> children = entry.getKey().getChildren();
            if (children != null) {
                applyChildren(entry.getValue(), children.stream().map(copies::get).collect(Collectors.toList()));
            }
        }
        return copies;
    }

    private Mono<Void> init(Flux<E> source, boolean partialUpdate) {
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
                .flatMap(list ->
                             queryIncludeChildren(list)
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

                if (partialUpdate) {
                    completeUpdateInput();
                } else {
                    // null 不表达移根；嵌套 children 已在展开时赋予明确父级，优先保留。
                    thisTime.values().forEach(node -> {
                        E previous = oldData.get(node.getId());
                        if (node.getParentId() == null && previous != null) {
                            node.setParentId(previous.getParentId());
                        }
                    });
                }
                readyToSave.putAll(thisTime);

                allData.putAll(includeChildren);
                allData.putAll(this.thisTime);
                initChildren();

            })
            .then();
    }

    private void completeUpdateInput() {
        for (E node : thisTime.values()) {
            E previous = oldData.get(node.getId());
            E patch = updateData.get(node.getId());
            if (previous != null) {
                // 仅本次持久图补齐遗漏字段；首次存在性读取不能成为用户的移动意图。
                List<E> children = node.getChildren();
                Map<String, Object> updates = FastBeanCopier.copy(patch, new LinkedHashMap<>());
                updates.values().removeIf(Objects::isNull);
                updates.remove("children");
                FastBeanCopier.copy(previous, node);
                FastBeanCopier.copy(updates, node);
                applyChildren(node, children);
            } else {
                // 初次展开早于持久父级补全；新节点的默认排序需按补全后的父级计算。
                node.setSortIndex(patch.getSortIndex());
            }
        }
        for (E root : updateRoots) {
            TreeSupportEntity.expandTree2List(root, getIdGenerator());
        }
    }

    /** 显式输入保留稀疏业务列；遗漏后代没有对应输入。 */
    protected E getUpdateData(E node) {
        E patch = updateData.get(node.getId());
        if (patch != null) {
            // 入口可能传入调用方稀疏对象，树字段统一取当前已准备的工作节点。
            node = allData.get(node.getId());
            E previous = oldData.get(node.getId());
            // 业务 patch 不回写未变化的隐式树字段，避免覆盖另一个事务已提交的移动。
            // 显式树输入（包括同值）及准备后的实际变化仍提交完整 path/level。
            boolean treeFieldsRequired = previous == null
                || patch.getParentId() != null
                || patch.getPath() != null
                || patch.getLevel() != null
                || CollectionUtils.isNotEmpty(patch.getChildren())
                || !Objects.equals(previous.getPath(), node.getPath())
                || !Objects.equals(previous.getLevel(), node.getLevel());
            if (treeFieldsRequired) {
                patch.setPath(node.getPath());
                patch.setLevel(node.getLevel());
            }
            if (isNewNode(node)) {
                patch.setSortIndex(node.getSortIndex());
            }
            // helper 已展开全部显式节点，逐节点保存不能再次递归写入 children。
            applyChildren(patch, Collections.emptyList());
        }
        return patch;
    }

    protected boolean isNewNode(E node) {
        return !oldData.containsKey(node.getId());
    }

    /** 新节点的业务 save 重新准备路径后，只同步尚待写入后代的树字段。 */
    void updateSavedNodePath(E saved) {
        E prepared = allData.get(saved.getId());
        prepared.setPath(saved.getPath());
        prepared.setLevel(saved.getLevel());
        refactorChildPath(saved.getId(), saved.getPath(), this::refactor);
    }

    private void initChildren() {
        childrenMapping.clear();

        for (E value : allData.values()) {
            if (isRootNode(value) || value.getId() == null) {
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
            .filter(value -> !isRootNode(value))
            .map(TreeSupportEntity::getParentId)
            .filter(e -> !ObjectUtils.isEmpty(e) && !allData.containsKey(e))
            .collect(Collectors.toSet());

        if (readyToCheck.isEmpty()) {
            return Mono.empty();
        }
        return queryById(readyToCheck)
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

        // flat 输入可能先给子节点；沿用已校验的父子映射，先准备父路径再处理后代。
        Queue<E> queue = new ArrayDeque<>();
        for (E node : allData.values()) {
            if (isRootNode(node) || !allData.containsKey(node.getParentId())) {
                queue.add(node);
            }
        }
        for (E node = queue.poll(); node != null; node = queue.poll()) {
            E data = thisTime.get(node.getId());
            if (data != null) {
                refactorPath(data, childGetter);
            }
            queue.addAll(childGetter.apply(node.getId()));
        }
    }

    private void refactorPath(E data, Function<PK, Collection<E>> childGetter) {
        E old = data.getId() == null ? null : oldData.get(data.getId());
        PK parentId = old != null ? old.getParentId() : data.getParentId();
        E oldParent = parentId == null ? null : allData.get(parentId);
        //编辑节点
        if (old != null) {
            Consumer<E> childConsumer = child -> {
                //更新了父节点,但是同时也传入的对应的子节点
                E readyToUpdate = thisTime.get(child.getId());
                if (null != readyToUpdate) {
                    readyToUpdate.setPath(child.getPath());
                }
            };
            PK newParentId = data.getParentId();
            //父节点发生变化，更新所有子节点path
            if (!Objects.equals(newParentId, parentId)) {
                //变更到了顶级节点
                if (isRootNode(data)) {
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
                        //父ID未变但祖先已移动；自身重建path时，遗漏输入的后代也必须保持一致。
                        data.setPath(oldParent.getPath() + "-" + RandomUtil.randomChar(4));
                        this.refactorChildPath(data.getId(), data.getPath(), childConsumer);
                        putChildToReadyToSave(childGetter, data);
                    }
                } else {
                    data.setPath(old.getPath());
                }
            }
        }

        //新增节点
        else if (parentId != null) {
            if (oldParent != null) {
                // 普通新增不能信任调用方传入的路径，即使形状符合当前父级也必须重新分配。
                data.setPath(oldParent.getPath() + "-" + RandomUtil.randomChar(4));
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
