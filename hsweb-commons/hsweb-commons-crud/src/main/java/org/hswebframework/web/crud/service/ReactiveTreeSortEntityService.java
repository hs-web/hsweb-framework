package org.hswebframework.web.crud.service;

import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveQuery;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.ezorm.rdb.operator.dml.Terms;
import org.hswebframework.web.api.crud.entity.*;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.crud.utils.TransactionUtils;
import org.reactivestreams.Publisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

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
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
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
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
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
     * @return 包含子节点的所有节点
     */
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> queryIncludeChildren(Collection<K> idList) {
        return queryIncludeChildren(findById(idList));
    }

    /**
     * 根据实体流查询全部子节点（包含原节点）
     *
     * @param entities 实体流
     * @return 包含子节点的所有节点
     * @since 4.0.18
     */
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> queryIncludeChildren(Flux<E> entities) {
        return entities
            .concatMap(e -> !StringUtils.hasText(e.getPath())
                ? Mono.just(e)
                : applyTreeScope(e, createQuery())
                    .like$("path", e.getPath())
                    .fetch(), Integer.MAX_VALUE)
            .distinct(TreeSupportEntity::getId);
    }

    /**
     * 查询指定ID的实体以及对应的全部父节点
     *
     * @param idList ID集合
     * @return 包含父节点的所有节点
     */
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> queryIncludeParent(Collection<K> idList) {
        return queryIncludeParent(findById(idList));
    }

    /**
     * 根据实体流查询全部父节点（包含原节点）
     *
     * @param entities 实体流
     * @return 包含父节点的所有节点
     * @since 4.0.18
     */
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> queryIncludeParent(Flux<E> entities) {
        return entities
            .concatMap(e -> !StringUtils.hasText(e.getPath())
                ? Mono.just(e)
                : applyTreeScope(e, createQuery())
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
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Flux<E> queryIncludeChildren(QueryParamEntity queryParam) {
        QueryParamEntity seeds = queryParam.clone();
        seeds.setIncludes(new HashSet<>());
        seeds.setExcludes(new HashSet<>());
        // 分组与去重必须使用完整内部节点，调用方的投影只应用于最终输出。
        Flux<E> fullNodes = queryIncludeChildren(query(seeds));
        if (queryParam.getIncludes().isEmpty() && queryParam.getExcludes().isEmpty()) {
            return fullNodes;
        }
        return fullNodes.map(TreeSupportEntity::getId).buffer(getBufferSize())
            .concatMap(ids -> createQuery().in("id", ids).as(q -> {
                if (CollectionUtils.isNotEmpty(queryParam.getIncludes())) {
                    q.select(queryParam.getIncludes().toArray(new String[0]));
                }
                if (CollectionUtils.isNotEmpty(queryParam.getExcludes())) {
                    q.selectExcludes(queryParam.getExcludes().toArray(new String[0]));
                }
                return q;
            }).fetch());
    }

    /**
     * 为同一数据表中的独立树追加分组条件，祖先、子树查询及子树删除统一使用此扩展点。
     * 默认不追加条件，保持已有单树服务的行为。分组实现应只追加条件，不修改实体或执行查询；
     * 传入实体必须包含分组字段，不能仅依靠随机 path 判断归属。
     *
     * @param node 当前树节点
     * @param query 已创建的查询
     * @return 追加同组约束后的查询
     * @since 5.0.2
     */
    default ReactiveQuery<E> applyTreeScope(E node, ReactiveQuery<E> query) {
        return query;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> insert(Publisher<E> entityPublisher) {
        return insertBatch(Flux.from(entityPublisher).collectList());
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> insert(E data) {
        return this.insertBatch(Flux.just(Collections.singletonList(data)));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> insertBatch(Publisher<? extends Collection<E>> entityPublisher) {
        Mono<Integer> operation = Mono.defer(() -> {
            // 每次订阅独立准备；前一批 SQL 和异步实体事件完成后，才处理下一批。
            ReactiveTreeSortServiceHelper<E, K> helper = new ReactiveTreeSortServiceHelper<>(this);
            return helper.prepare(Flux.from(entityPublisher).flatMapIterable(Function.identity()))
                .buffer(getBufferSize())
                .concatMap(batch -> getRepository().insertBatch(Mono.just(batch)))
                .reduce(Math::addExact)
                .defaultIfEmpty(0);
        });
        return TransactionUtils.tryRunInTransaction(operation, new DefaultTransactionDefinition());
    }

    default int getBufferSize() {
        return 200;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class,
        transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<SaveResult> save(Publisher<E> entityPublisher) {
        Mono<SaveResult> operation = Mono.defer(() -> {
            // 每次订阅独立准备树快照；helper 不在服务单例或重复订阅之间共享。
            ReactiveTreeSortServiceHelper<E, K> helper = new ReactiveTreeSortServiceHelper<>(this);
            return helper.prepare(Flux.from(entityPublisher))
                .buffer(getBufferSize())
                .concatMap(batch -> Flux.fromIterable(batch)
                    .filter(helper::isMovingToRoot)
                    // ORM save 忽略普通 null；仅既有节点显式移根时，在原 save 前清空父级。
                    // 同时写入已准备的 path/level，modify 与随后的 save 事件共享原事务。
                    .concatMap(node -> getRepository().createUpdate()
                        .set("path", node.getPath())
                        .set("level", node.getLevel())
                        .setNull("parentId")
                        .where("id", node.getId())
                        .execute())
                    .then(getRepository().save(batch)))
                .reduce(SaveResult::merge);
        });
        return TransactionUtils.tryRunInTransaction(operation, new DefaultTransactionDefinition());

    }

    @Deprecated
    default Flux<E> tryRefactorPath(Flux<E> stream) {
        return new ReactiveTreeSortServiceHelper<>(this).prepare(stream);
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
        return entityPublisher.flatMap(data -> findById(id).flatMap(existing -> {
            // 部分修改忽略未提供的 null；显式移根保存完整状态，或在有树生命周期维护的服务中使用 DSL setNull。
            Map<String, Object> updates = FastBeanCopier.copy(data, new LinkedHashMap<>());
            updates.values().removeIf(Objects::isNull);
            updates.remove("id");
            FastBeanCopier.copy(updates, existing);
            existing.setId(id);
            return save(existing).map(SaveResult::getTotal);
        })).defaultIfEmpty(0);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> updateById(K id, E entity) {
        return updateById(id, Mono.just(entity));
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> deleteById(K id) {
        return this.deleteById(Flux.just(id));
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    default Mono<Integer> deleteById(Publisher<K> idPublisher) {
        return Flux.from(idPublisher).collectList().flatMap(ids -> ids.isEmpty()
            ? Mono.just(0)
            : createDelete().in("id", ids).execute());
    }

    IDGenerator<K> getIDGenerator();

    void setChildren(E entity, List<E> children);

    default List<E> getChildren(E entity) {
        return entity.getChildren();
    }

    default Predicate<E> createRootNodePredicate(TreeSupportEntity.TreeHelper<E, K> helper) {
        return node -> {
            //有父节点,但是父节点不存在
            if (!ObjectUtils.isEmpty(node.getParentId())) {
                return helper.getNode(node.getParentId()) == null;
            }
            return isRootNode(node);
        };
    }

    default boolean isRootNode(E entity) {
        return ObjectUtils.isEmpty(entity.getParentId()) || "-1".equals(String.valueOf(entity.getId()));
    }

    @Override
    default ReactiveDelete createDelete() {
        return ReactiveCrudService.super.createDelete().onExecute((delete, executor) ->
            TransactionUtils.tryRunInTransaction(
                // 完整实体用于分组和层级排序；叶先有界分片，每批SQL与事件完成后才删除父级。
                // 全部批次仍参与同一个原事务，后批事件或调用方失败必须整体回滚。
                queryIncludeChildren(delete.toQueryParam(QueryParamEntity::new))
                    .sort(Comparator.comparing((E node) -> node.getLevel(),
                                               Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
                    .map(TreeSupportEntity::getId)
                    .buffer(getBufferSize())
                    .concatMap(ids -> getRepository().createDelete().in("id", ids).execute())
                    .reduce(Math::addExact)
                    .defaultIfEmpty(0),
                new DefaultTransactionDefinition()));
    }
}
