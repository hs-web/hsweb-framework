package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveQuery;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.crud.TestApplication;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.crud.events.EntityBeforeCreateEvent;
import org.hswebframework.web.crud.events.EntityBeforeDeleteEvent;
import org.hswebframework.web.crud.events.EntityBeforeSaveEvent;
import org.hswebframework.web.crud.events.EntityCreatedEvent;
import org.hswebframework.web.crud.events.EntityDeletedEvent;
import org.hswebframework.web.crud.events.EntityModifyEvent;
import org.hswebframework.web.crud.events.EntitySavedEvent;
import org.hswebframework.web.crud.utils.TransactionUtils;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.exception.ValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/** 公共树分组、旧单树部分修改及真实实体事件事务的数据库回归。 */
@SpringBootTest(classes = {TestApplication.class, TestTreeSortEntityService.class,
    ScopedReactiveTreeSortEntityServiceTest.Configuration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class ScopedReactiveTreeSortEntityServiceTest {

    @Autowired
    @Qualifier("scopedTreeService")
    private ScopedTreeService scoped;

    @Autowired
    private TestTreeSortEntityService unscoped;

    @Autowired
    private FailureListener listener;

    @Autowired
    private BatchedTreeService batched;

    @Test
    public void samePathsInDifferentGroupsStayIsolatedForParentsChildrenAndDeletion() {
        String groupA = id();
        String groupB = id();
        TestTreeSortEntity rootA = node(groupA, null, "same");
        TestTreeSortEntity rootB = node(groupB, null, "same");
        TestTreeSortEntity childA = node(groupA, rootA.getId(), "same-chld");
        TestTreeSortEntity childB = node(groupB, rootB.getId(), "same-chld");
        // 直接保存刻意同path的历史数据，验证隔离不能依赖随机生成器的低碰撞概率。
        scoped.getRepository().save(List.of(rootA, rootB, childA, childB))
            .as(StepVerifier::create).expectNextCount(1).verifyComplete();
        scoped.queryIncludeChildren(List.of(rootA.getId(), rootB.getId())).collectList()
            .as(StepVerifier::create).assertNext(nodes -> assertEquals(
                Set.of(rootA.getId(), rootB.getId(), childA.getId(), childB.getId()), ids(nodes)))
            .verifyComplete();
        scoped.queryIncludeParent(List.of(childA.getId())).collectList()
            .as(StepVerifier::create).assertNext(nodes ->
                assertEquals(Set.of(rootA.getId(), childA.getId()), ids(nodes))).verifyComplete();
        scoped.deleteById(rootA.getId()).as(StepVerifier::create).expectNext(2).verifyComplete();
        scoped.findById(List.of(rootB.getId(), childB.getId())).count()
            .as(StepVerifier::create).expectNext(2L).verifyComplete();
        scoped.createDelete().where("id", rootB.getId()).execute()
            .as(StepVerifier::create).expectNext(2).verifyComplete();
    }

    @Test
    public void projectionsDoNotRemoveInternalTreeScopeOrChangeRequestedFields() {
        String groupA = id();
        String groupB = id();
        TestTreeSortEntity rootA = node(groupA, null, "same");
        TestTreeSortEntity rootB = node(groupB, null, "same");
        TestTreeSortEntity rootNull = node(null, null, "same");
        TestTreeSortEntity childA = node(groupA, rootA.getId(), "same-chld");
        TestTreeSortEntity childB = node(groupB, rootB.getId(), "same-chld");
        TestTreeSortEntity childNull = node(null, rootNull.getId(), "same-chld");
        scoped.getRepository().save(List.of(rootA, rootB, rootNull, childA, childB, childNull))
            .as(StepVerifier::create).expectNextCount(1).verifyComplete();
        QueryParamEntity included = QueryParamEntity.of("id", rootA.getId()).includes("id", "name");
        scoped.queryIncludeChildren(included).collectList().as(StepVerifier::create).assertNext(nodes -> {
            assertEquals(Set.of(rootA.getId(), childA.getId()), ids(nodes));
            assertTrue(nodes.stream().allMatch(node -> node.getGroupId() == null && node.getPath() == null));
        }).verifyComplete();
        assertEquals(Set.of("id", "name"), included.getIncludes());
        QueryParamEntity excluded = QueryParamEntity.of("id", rootA.getId()).excludes("groupId");
        scoped.queryIncludeChildren(excluded).collectList().as(StepVerifier::create).assertNext(nodes -> {
            assertEquals(Set.of(rootA.getId(), childA.getId()), ids(nodes));
            assertTrue(nodes.stream().allMatch(node -> node.getGroupId() == null));
        }).verifyComplete();
        assertEquals(Set.of("groupId"), excluded.getExcludes());
        QueryParamEntity namesOnly = QueryParamEntity.of("id", rootA.getId()).includes("name");
        scoped.queryIncludeChildren(namesOnly)
            .map(TestTreeSortEntity::getName).collectList().as(StepVerifier::create)
            .assertNext(names -> assertEquals(Set.of(rootA.getName(), childA.getName()), Set.copyOf(names)))
            .verifyComplete();
        // 空分组也是真实独立树；清理使用精确ID，不让普通无scope服务跨组按path删除。
        scoped.getRepository().createDelete().in("id", List.of(rootA.getId(), rootB.getId(), rootNull.getId(),
            childA.getId(), childB.getId(), childNull.getId())).execute()
            .as(StepVerifier::create).expectNext(6).verifyComplete();
    }

    @Test
    public void defaultSingleTreeNamePatchesPreserveParentAndOrdinaryDelete() {
        TestTreeSortEntity root = node(null, null, null);
        TestTreeSortEntity child = node(null, root.getId(), null);
        root.setChildren(List.of(child));
        unscoped.save(root).as(StepVerifier::create).expectNextCount(1).verifyComplete();
        TestTreeSortEntity firstPatch = new TestTreeSortEntity();
        firstPatch.setName("renamed first");
        unscoped.updateById(child.getId(), firstPatch)
            .as(StepVerifier::create).expectNext(1).verifyComplete();
        TestTreeSortEntity secondPatch = new TestTreeSortEntity();
        secondPatch.setName("renamed second");
        unscoped.updateById(child.getId(), Mono.just(secondPatch)).then(unscoped.findById(child.getId()))
            .as(StepVerifier::create).assertNext(after -> {
                assertEquals("renamed second", after.getName());
                assertEquals(root.getId(), after.getParentId());
                assertTrue(after.getPath().startsWith(root.getPath() + "-"));
            }).verifyComplete();
        unscoped.deleteById(root.getId()).as(StepVerifier::create).expectNext(2).verifyComplete();
    }

    @Test
    public void fullEntitySaveMovesWholeSubtreeToRoot() {
        TestTreeSortEntity root = node(null, null, null);
        TestTreeSortEntity branch = node(null, root.getId(), null);
        branch.setName("verify-root-" + id());
        int observedRoots = listener.observedRoots.get();
        TestTreeSortEntity leaf = node(null, branch.getId(), null);
        root.setChildren(List.of(branch));
        branch.setChildren(List.of(leaf));
        unscoped.save(root).as(StepVerifier::create).expectNextCount(1).verifyComplete();
        unscoped.findById(branch.getId()).flatMap(existing -> {
            existing.setParentId(null);
            return unscoped.save(existing);
        }).then(unscoped.queryIncludeChildren(List.of(root.getId())).count())
            .as(StepVerifier::create).expectNext(1L).verifyComplete();
        unscoped.queryIncludeChildren(List.of(branch.getId())).collectList()
            .as(StepVerifier::create).assertNext(nodes ->
                assertEquals(Set.of(branch.getId(), leaf.getId()), ids(nodes))).verifyComplete();
        Mono.zip(unscoped.findById(branch.getId()), unscoped.findById(leaf.getId()))
            .as(StepVerifier::create).assertNext(nodes -> {
                assertNull(nodes.getT1().getParentId());
                assertEquals(Integer.valueOf(1), nodes.getT1().getLevel());
                assertEquals(branch.getId(), nodes.getT2().getParentId());
                assertEquals(Integer.valueOf(2), nodes.getT2().getLevel());
            }).verifyComplete();
        assertEquals(observedRoots + 1, listener.observedRoots.get());
        unscoped.createDelete().in("id", List.of(root.getId(), branch.getId())).execute()
            .as(StepVerifier::create).expectNext(3).verifyComplete();
    }

    @Test
    public void explicitRootClearAndTreeSaveRollbackWithEventsAndCallerTransaction() {
        for (String failure : List.of("modify", "save", "caller")) {
            TestTreeSortEntity root = node(null, null, null);
            TestTreeSortEntity branch = node(null, root.getId(), null);
            branch.setName("reject-root-" + failure + "-" + id());
            TestTreeSortEntity leaf = node(null, branch.getId(), null);
            root.setChildren(List.of(branch));
            branch.setChildren(List.of(leaf));
            unscoped.save(root).as(StepVerifier::create).expectNextCount(1).verifyComplete();
            String branchPath = branch.getPath();
            String leafPath = leaf.getPath();
            Mono<?> move = unscoped.findById(branch.getId()).flatMap(existing -> {
                existing.setParentId(null);
                return unscoped.save(existing);
            });
            if ("caller".equals(failure)) {
                move = TransactionUtils.tryRunInTransaction(
                    move.then(Mono.error(new IllegalStateException("reject source transaction"))),
                    new DefaultTransactionDefinition());
            }
            move.as(StepVerifier::create).expectError(IllegalStateException.class).verify();
            Mono.zip(unscoped.findById(branch.getId()), unscoped.findById(leaf.getId()))
                .as(StepVerifier::create).assertNext(nodes -> {
                    assertEquals(root.getId(), nodes.getT1().getParentId());
                    assertEquals(branchPath, nodes.getT1().getPath());
                    assertEquals(Integer.valueOf(2), nodes.getT1().getLevel());
                    assertEquals(branch.getId(), nodes.getT2().getParentId());
                    assertEquals(leafPath, nodes.getT2().getPath());
                    assertEquals(Integer.valueOf(3), nodes.getT2().getLevel());
                }).verifyComplete();
            unscoped.deleteById(root.getId()).as(StepVerifier::create).expectNext(3).verifyComplete();
        }
    }

    @Test
    public void dslSqlAndRealEntityEventsRollbackTogether() {
        TestTreeSortEntity root = node(id(), null, null);
        TestTreeSortEntity child = node(root.getGroupId(), root.getId(), null);
        root.setChildren(List.of(child));
        scoped.save(root).as(StepVerifier::create).expectNextCount(1).verifyComplete();
        String before = root.getName();
        scoped.createUpdate().set("name", "reject-dsl-" + id()).where("id", root.getId()).execute()
            .as(StepVerifier::create).expectError(IllegalStateException.class).verify();
        scoped.findById(root.getId()).as(StepVerifier::create)
            .assertNext(existing -> assertEquals(before, existing.getName())).verifyComplete();
        scoped.createUpdate().set("name", "reject-delete-" + id()).where("id", root.getId()).execute()
            .as(StepVerifier::create).expectNext(1).verifyComplete();
        scoped.createDelete().where("id", root.getId()).execute()
            .as(StepVerifier::create).expectError(IllegalStateException.class).verify();
        scoped.findById(List.of(root.getId(), child.getId())).count()
            .as(StepVerifier::create).expectNext(2L).verifyComplete();
        // 使用原生产事件链恢复正常状态后清理，不直接调用listener。
        scoped.createUpdate().set("name", before).where("id", root.getId()).execute()
            .then(scoped.deleteById(root.getId())).as(StepVerifier::create).expectNext(2).verifyComplete();
    }

    @Test
    public void preparedFlatTreesUseParentDependenciesAndKeepSameLevelInputOrder() {
        String group = id();
        TestTreeSortEntity rootA = node(group, null, null);
        TestTreeSortEntity rootB = node(group, null, null);
        TestTreeSortEntity branchA = node(group, rootA.getId(), null);
        TestTreeSortEntity branchB = node(group, rootB.getId(), null);
        TestTreeSortEntity leafA = node(group, branchA.getId(), null);
        TestTreeSortEntity leafB = node(group, branchB.getId(), null);
        new ReactiveTreeSortServiceHelper<>(batched)
            .prepare(Flux.fromIterable(List.of(leafB, branchA, rootB, leafA, branchB, rootA)))
            .collectList().as(StepVerifier::create).assertNext(prepared -> {
                assertEquals(List.of(rootB.getId(), rootA.getId(), branchA.getId(), branchB.getId(),
                    leafB.getId(), leafA.getId()), prepared.stream().map(TestTreeSortEntity::getId)
                    .collect(Collectors.toList()));
                assertTreeStructure(prepared);
            }).verifyComplete();
        batched.insertBatch(Flux.empty()).as(StepVerifier::create).expectNext(0).verifyComplete();
    }

    @Test
    public void smallBatchesCompleteParentEventsBeforeChildrenAndMoveToExistingRoot() {
        for (String operation : List.of("insert", "save")) {
            String group = id();
            TestTreeSortEntity rootA = batchNode(group, null);
            TestTreeSortEntity rootB = batchNode(group, null);
            TestTreeSortEntity branchA = batchNode(group, rootA.getId());
            TestTreeSortEntity branchB = batchNode(group, rootB.getId());
            TestTreeSortEntity leafA = batchNode(group, branchA.getId());
            TestTreeSortEntity leafB = batchNode(group, branchB.getId());
            List<TestTreeSortEntity> flat = List.of(leafB, branchA, rootB, leafA, branchB, rootA);
            Mono<?> write = "insert".equals(operation)
                ? batched.insertBatch(Mono.just(flat)) : batched.save(Flux.fromIterable(flat));
            write.then(batched.findById(flat.stream().map(TestTreeSortEntity::getId)
                .collect(Collectors.toList())).collectList()).as(StepVerifier::create)
                .assertNext(stored -> {
                    assertEquals(6, stored.size());
                    assertTreeStructure(stored);
                    assertTrue(stored.stream().allMatch(node -> "tree-event-complete".equals(node.getDefaultTest())));
                }).verifyComplete();
            // 目标根未包含在本次保存中；原图叶节点也必须随分支一同移到新树。
            batched.findById(branchA.getId()).flatMap(branch -> {
                branch.setParentId(rootB.getId());
                return batched.save(branch);
            }).then(batched.findById(flat.stream().map(TestTreeSortEntity::getId)
                .collect(Collectors.toList())).collectList()).as(StepVerifier::create)
                .assertNext(ScopedReactiveTreeSortEntityServiceTest::assertTreeStructure).verifyComplete();
            batched.queryIncludeChildren(List.of(rootA.getId())).count()
                .as(StepVerifier::create).expectNext(1L).verifyComplete();
            batched.queryIncludeChildren(List.of(rootB.getId())).count()
                .as(StepVerifier::create).expectNext(5L).verifyComplete();
            batched.getRepository().createDelete().where("groupId", group).execute()
                .as(StepVerifier::create).expectNext(6).verifyComplete();
        }
    }

    @Test
    public void laterBatchEventsAndCallerFailureRollbackEveryBatchAndOriginalWrite() {
        for (String operation : List.of("insert", "save")) {
            for (String failure : List.of("event", "caller")) {
                String group = id();
                TestTreeSortEntity original = node(group, null, null);
                TestTreeSortEntity root = batchNode(group, null);
                TestTreeSortEntity branch = batchNode(group, root.getId());
                TestTreeSortEntity leaf = batchNode(group, branch.getId());
                if ("event".equals(failure)) {
                    leaf.setName("reject-batch-event-" + id());
                }
                List<TestTreeSortEntity> flat = List.of(leaf, branch, root);
                Mono<?> write = "insert".equals(operation)
                    ? batched.insertBatch(Mono.just(flat)) : batched.save(Flux.fromIterable(flat));
                Mono<?> transaction = batched.getRepository().save(original).then(write);
                if ("caller".equals(failure)) {
                    transaction = transaction.then(Mono.error(new IllegalStateException("reject original transaction")));
                }
                TransactionUtils.tryRunInTransaction(transaction, new DefaultTransactionDefinition())
                    .as(StepVerifier::create).expectError(IllegalStateException.class).verify();
                batched.createQuery().where("groupId", group).count()
                    .as(StepVerifier::create).expectNext(0).verifyComplete();
            }
        }
    }

    @Test
    public void savingParentAndChildTogetherKeepsOmittedGrandchildInMovedSubtree() {
        for (boolean childFirst : List.of(false, true)) {
            for (boolean moveToRoot : List.of(false, true)) {
                String group = id();
                TestTreeSortEntity root = node(group, null, null);
                TestTreeSortEntity target = node(group, null, null);
                TestTreeSortEntity branch = node(group, root.getId(), null);
                TestTreeSortEntity child = node(group, branch.getId(), null);
                TestTreeSortEntity grandchild = node(group, child.getId(), null);
                branch.setSortIndex(17L);
                child.setSortIndex(23L);
                grandchild.setSortIndex(31L);
                scoped.save(List.of(root, target, branch, child, grandchild))
                    .as(StepVerifier::create).expectNextCount(1).verifyComplete();
                Mono.zip(scoped.findById(branch.getId()), scoped.findById(child.getId()))
                    .flatMap(pair -> {
                        TestTreeSortEntity moved = pair.getT1();
                        TestTreeSortEntity suppliedChild = pair.getT2();
                        moved.setParentId(moveToRoot ? null : target.getId());
                        // 两节点来自数据库，children为空；孙节点必须由持久图加载并随路径修复。
                        assertNull(moved.getChildren());
                        assertNull(suppliedChild.getChildren());
                        return scoped.save(childFirst ? List.of(suppliedChild, moved)
                            : List.of(moved, suppliedChild));
                    }).then(stored(scoped, group)).as(StepVerifier::create).assertNext(tree -> {
                        assertEquals(5, tree.size());
                        assertTreeStructure(tree.values());
                        TestTreeSortEntity moved = tree.get(branch.getId());
                        assertEquals(moveToRoot ? null : target.getId(), moved.getParentId());
                        assertEquals(Integer.valueOf(moveToRoot ? 1 : 2), moved.getLevel());
                        assertEquals(root.getPath(), tree.get(root.getId()).getPath());
                        assertEquals(target.getPath(), tree.get(target.getId()).getPath());
                        assertEquals(Long.valueOf(17), moved.getSortIndex());
                        assertEquals(Long.valueOf(23), tree.get(child.getId()).getSortIndex());
                        assertEquals(Long.valueOf(31), tree.get(grandchild.getId()).getSortIndex());
                    }).verifyComplete();
                scoped.queryIncludeChildren(List.of(child.getId())).collectList()
                    .as(StepVerifier::create).assertNext(nodes ->
                        assertEquals(Set.of(child.getId(), grandchild.getId()), ids(nodes))).verifyComplete();
                scoped.deleteById(child.getId()).as(StepVerifier::create).expectNext(2).verifyComplete();
                stored(scoped, group).as(StepVerifier::create).assertNext(tree -> {
                    assertEquals(Set.of(root.getId(), target.getId(), branch.getId()), tree.keySet());
                    assertTreeStructure(tree.values());
                }).verifyComplete();
                scoped.getRepository().createDelete().where("groupId", group).execute()
                    .as(StepVerifier::create).expectNext(3).verifyComplete();
            }
        }
    }

    @Test
    public void suppliedParentAndChildCanMoveToDifferentExistingParents() {
        for (boolean childFirst : List.of(false, true)) {
            String group = id();
            TestTreeSortEntity root = node(group, null, null);
            TestTreeSortEntity parentTarget = node(group, null, null);
            TestTreeSortEntity childTarget = node(group, parentTarget.getId(), null);
            TestTreeSortEntity branch = node(group, root.getId(), null);
            TestTreeSortEntity child = node(group, branch.getId(), null);
            TestTreeSortEntity grandchild = node(group, child.getId(), null);
            scoped.save(List.of(root, parentTarget, childTarget, branch, child, grandchild))
                .as(StepVerifier::create).expectNextCount(1).verifyComplete();
            Mono.zip(scoped.findById(branch.getId()), scoped.findById(child.getId()))
                .flatMap(pair -> {
                    pair.getT1().setParentId(parentTarget.getId());
                    pair.getT2().setParentId(childTarget.getId());
                    return scoped.save(childFirst ? List.of(pair.getT2(), pair.getT1())
                        : List.of(pair.getT1(), pair.getT2()));
                }).then(stored(scoped, group)).as(StepVerifier::create).assertNext(tree -> {
                    assertEquals(6, tree.size());
                    assertTreeStructure(tree.values());
                    assertEquals(parentTarget.getId(), tree.get(branch.getId()).getParentId());
                    assertEquals(childTarget.getId(), tree.get(child.getId()).getParentId());
                    assertEquals(child.getId(), tree.get(grandchild.getId()).getParentId());
                    assertEquals(root.getPath(), tree.get(root.getId()).getPath());
                    assertEquals(parentTarget.getPath(), tree.get(parentTarget.getId()).getPath());
                    assertEquals(childTarget.getPath(), tree.get(childTarget.getId()).getPath());
                }).verifyComplete();
            scoped.queryIncludeChildren(List.of(branch.getId())).collectList()
                .as(StepVerifier::create).assertNext(nodes ->
                    assertEquals(Set.of(branch.getId()), ids(nodes))).verifyComplete();
            scoped.queryIncludeChildren(List.of(childTarget.getId())).collectList()
                .as(StepVerifier::create).assertNext(nodes ->
                    assertEquals(Set.of(childTarget.getId(), child.getId(), grandchild.getId()), ids(nodes)))
                .verifyComplete();
            scoped.getRepository().createDelete().where("groupId", group).execute()
                .as(StepVerifier::create).expectNext(6).verifyComplete();
        }
    }

    @Test
    public void externalDeepParentSupportsExistingMovesAndNewFlatBranches() {
        String group = id();
        TestTreeSortEntity targetRoot = node(group, null, null);
        TestTreeSortEntity targetBranch = node(group, targetRoot.getId(), null);
        TestTreeSortEntity target = node(group, targetBranch.getId(), null);
        TestTreeSortEntity oldRoot = node(group, null, null);
        TestTreeSortEntity branch = node(group, oldRoot.getId(), null);
        TestTreeSortEntity leaf = node(group, branch.getId(), null);
        scoped.save(List.of(targetRoot, targetBranch, target, oldRoot, branch, leaf))
            .as(StepVerifier::create).expectNextCount(1).verifyComplete();
        scoped.findById(branch.getId()).flatMap(existing -> {
            existing.setParentId(target.getId());
            return scoped.save(existing);
        }).then(stored(scoped, group)).as(StepVerifier::create).assertNext(tree -> {
            assertTreeStructure(tree.values());
            assertEquals(Integer.valueOf(4), tree.get(branch.getId()).getLevel());
            assertEquals(Integer.valueOf(5), tree.get(leaf.getId()).getLevel());
        }).verifyComplete();
        for (String operation : List.of("insert", "save")) {
            TestTreeSortEntity added = node(group, target.getId(), null);
            TestTreeSortEntity addedLeaf = node(group, added.getId(), null);
            // 只提供新分支且子先父后；已有level3目标及其祖先均不在输入中。
            Mono<?> write = "insert".equals(operation) ? scoped.insert(Flux.just(addedLeaf, added))
                : scoped.save(Flux.just(addedLeaf, added));
            write.then(stored(scoped, group)).as(StepVerifier::create).assertNext(tree -> {
                assertTreeStructure(tree.values());
                assertEquals(Integer.valueOf(4), tree.get(added.getId()).getLevel());
                assertEquals(Integer.valueOf(5), tree.get(addedLeaf.getId()).getLevel());
                assertEquals(targetRoot.getPath(), tree.get(targetRoot.getId()).getPath());
                assertEquals(targetBranch.getPath(), tree.get(targetBranch.getId()).getPath());
                assertEquals(target.getPath(), tree.get(target.getId()).getPath());
                assertEquals(oldRoot.getPath(), tree.get(oldRoot.getId()).getPath());
            }).verifyComplete();
        }
        scoped.deleteById(targetRoot.getId()).as(StepVerifier::create).expectNext(9).verifyComplete();
        scoped.deleteById(oldRoot.getId()).as(StepVerifier::create).expectNext(1).verifyComplete();
    }

    @Test
    public void repeatedSaveSubscriptionReadsFreshTreeAndEmptyWritesKeepExistingSemantics() {
        String group = id();
        TestTreeSortEntity root = node(group, null, null);
        TestTreeSortEntity target = node(group, null, null);
        TestTreeSortEntity branch = node(group, root.getId(), null);
        TestTreeSortEntity leaf = node(group, branch.getId(), null);
        scoped.save(List.of(root, target, branch, leaf))
            .as(StepVerifier::create).expectNextCount(1).verifyComplete();
        AtomicInteger subscriptions = new AtomicInteger();
        Mono<?> repeated = scoped.save(Flux.defer(() -> {
            int subscription = subscriptions.incrementAndGet();
            return scoped.findById(branch.getId()).doOnNext(existing -> {
                existing.setParentId(target.getId());
                existing.setName("subscription-" + subscription);
            });
        }));
        for (int subscription = 1; subscription <= 2; subscription++) {
            int expectedSubscription = subscription;
            repeated.then(stored(scoped, group)).as(StepVerifier::create).assertNext(tree -> {
                assertTreeStructure(tree.values());
                assertEquals(target.getId(), tree.get(branch.getId()).getParentId());
                assertEquals("subscription-" + expectedSubscription, tree.get(branch.getId()).getName());
                assertEquals(Integer.valueOf(3), tree.get(leaf.getId()).getLevel());
            }).verifyComplete();
            if (subscription == 1) {
                // 在两次订阅间改变真实持久图，复用Publisher不得沿用第一次helper快照。
                scoped.findById(branch.getId()).flatMap(existing -> {
                    existing.setParentId(null);
                    return scoped.save(existing);
                }).then(stored(scoped, group)).as(StepVerifier::create).assertNext(tree -> {
                    assertTreeStructure(tree.values());
                    assertNull(tree.get(branch.getId()).getParentId());
                    assertEquals(Integer.valueOf(2), tree.get(leaf.getId()).getLevel());
                }).verifyComplete();
            }
        }
        assertEquals(2, subscriptions.get());
        scoped.save(Flux.empty()).as(StepVerifier::create).verifyComplete();
        scoped.insertBatch(Flux.empty()).as(StepVerifier::create).expectNext(0).verifyComplete();
        scoped.getRepository().createDelete().where("groupId", group).execute()
            .as(StepVerifier::create).expectNext(4).verifyComplete();
    }

    @Test
    public void rejectedCyclesAndMissingParentsLeavePersistedTreeAndCallerWritesUnchanged() {
        for (String scenario : List.of("insert-self", "insert-batch", "save-self", "save-ancestor", "save-missing")) {
            String group = id();
            TestTreeSortEntity root = node(group, null, null);
            TestTreeSortEntity branch = node(group, root.getId(), null);
            TestTreeSortEntity leaf = node(group, branch.getId(), null);
            scoped.save(List.of(root, branch, leaf))
                .as(StepVerifier::create).expectNextCount(1).verifyComplete();
            AtomicReference<Map<String, TestTreeSortEntity>> before = new AtomicReference<>();
            stored(scoped, group).as(StepVerifier::create).assertNext(before::set).verifyComplete();
            TestTreeSortEntity unrelated = node(id(), null, null);
            Mono<?> invalid;
            if ("save-ancestor".equals(scenario) || "save-missing".equals(scenario)) {
                invalid = scoped.findById(root.getId()).flatMap(existing -> {
                    existing.setParentId("save-ancestor".equals(scenario) ? leaf.getId() : id());
                    return scoped.save(existing);
                });
            } else {
                TestTreeSortEntity first = node(group, null, null);
                TestTreeSortEntity second = node(group, first.getId(), null);
                first.setParentId(scenario.endsWith("self") ? first.getId() : second.getId());
                List<TestTreeSortEntity> input = scenario.endsWith("self") ? List.of(first) : List.of(second, first);
                invalid = scenario.startsWith("insert") ? scoped.insert(Flux.fromIterable(input))
                    : scoped.save(Flux.fromIterable(input));
            }
            Mono<?> caller = scoped.getRepository().save(unrelated)
                .then(scoped.getRepository().createUpdate().set("name", "must-roll-back")
                    .where("id", root.getId()).execute()).then(invalid);
            TransactionUtils.tryRunInTransaction(caller, new DefaultTransactionDefinition())
                .as(StepVerifier::create).expectErrorMatches(error -> error instanceof ValidationException
                    && error.getMessage().contains(scenario.endsWith("missing")
                        ? "tree_entity_parent_id_not_exist" : "tree_entity_cyclic_dependency")).verify();
            stored(scoped, group).as(StepVerifier::create).assertNext(after ->
                assertSnapshot(before.get(), after)).verifyComplete();
            scoped.findById(unrelated.getId()).as(StepVerifier::create).verifyComplete();
            scoped.deleteById(root.getId()).as(StepVerifier::create).expectNext(3).verifyComplete();
        }
    }

    @Test
    public void subtreeDeleteUsesBoundedLeafFirstBatchesAndRollsBackLaterFailures() {
        for (String outcome : List.of("success", "event", "caller")) {
            String group = id();
            TestTreeSortEntity rootA = deleteBatchNode(group, null);
            TestTreeSortEntity branchA = deleteBatchNode(group, rootA.getId());
            TestTreeSortEntity leafA = deleteBatchNode(group, branchA.getId());
            TestTreeSortEntity rootB = deleteBatchNode(group, null);
            TestTreeSortEntity leafB = deleteBatchNode(group, rootB.getId());
            if ("event".equals(outcome)) {
                rootB.setName("reject-delete-batch-later-" + rootB.getId());
            }
            batched.save(List.of(rootA, branchA, leafA, rootB, leafB))
                .as(StepVerifier::create).expectNextCount(1).verifyComplete();
            AtomicReference<Map<String, TestTreeSortEntity>> before = new AtomicReference<>();
            stored(batched, group).as(StepVerifier::create).assertNext(before::set).verifyComplete();
            TestTreeSortEntity unrelated = node(id(), null, null);
            Mono<Integer> deletion = batched.createDelete()
                .in("id", List.of(rootA.getId(), branchA.getId(), rootB.getId(), id())).execute();
            Mono<Integer> caller = batched.getRepository().save(unrelated).then(deletion);
            if ("caller".equals(outcome)) {
                caller = caller.flatMap(ignored -> Mono.error(new IllegalStateException("reject delete caller")));
            }
            Mono<Integer> transaction = TransactionUtils.tryRunInTransaction(caller, new DefaultTransactionDefinition());
            if ("success".equals(outcome)) {
                transaction.as(StepVerifier::create).expectNext(5).verifyComplete();
                stored(batched, group).as(StepVerifier::create).assertNext(tree -> assertTrue(tree.isEmpty()))
                    .verifyComplete();
                batched.getRepository().deleteById(unrelated.getId())
                    .as(StepVerifier::create).expectNext(1).verifyComplete();
            } else {
                transaction.as(StepVerifier::create).expectError(IllegalStateException.class).verify();
                stored(batched, group).as(StepVerifier::create).assertNext(after ->
                    assertSnapshot(before.get(), after)).verifyComplete();
                batched.findById(unrelated.getId()).as(StepVerifier::create).verifyComplete();
                batched.getRepository().createUpdate().set("name", "cleanup").where("groupId", group).execute()
                    .then(batched.getRepository().createDelete().where("groupId", group).execute())
                    .as(StepVerifier::create).expectNext(5).verifyComplete();
            }
            List<Integer> batches = listener.deleteBatches.get(group);
            assertNotNull(batches);
            assertTrue("must reach later delete batches", batches.size() >= 2);
            assertTrue(batches.stream().allMatch(size -> size <= batched.getBufferSize()));
            if ("success".equals(outcome)) {
                assertEquals(3, batches.size());
            }
        }
        batched.createDelete().where("id", id()).execute()
            .as(StepVerifier::create).expectNext(0).verifyComplete();
        batched.deleteById(Flux.empty()).as(StepVerifier::create).expectNext(0).verifyComplete();
    }

    @Test
    public void bothUpdateByIdParentPatchOverloadsMoveCompleteSubtreeToDeepParent() {
        for (String overload : List.of("entity", "mono")) {
            String group = id();
            TestTreeSortEntity targetRoot = node(group, null, null);
            TestTreeSortEntity targetBranch = node(group, targetRoot.getId(), null);
            TestTreeSortEntity target = node(group, targetBranch.getId(), null);
            TestTreeSortEntity root = node(group, null, null);
            TestTreeSortEntity branch = node(group, root.getId(), null);
            TestTreeSortEntity leaf = node(group, branch.getId(), null);
            branch.setSortIndex(17L);
            leaf.setSortIndex(23L);
            scoped.save(List.of(targetRoot, targetBranch, target, root, branch, leaf))
                .as(StepVerifier::create).expectNextCount(1).verifyComplete();
            TestTreeSortEntity patch = new TestTreeSortEntity();
            patch.setParentId(target.getId());
            Mono<Integer> move = "entity".equals(overload) ? scoped.updateById(branch.getId(), patch)
                : scoped.updateById(branch.getId(), Mono.just(patch));
            move.as(StepVerifier::create).expectNext(2).verifyComplete();
            stored(scoped, group).as(StepVerifier::create).assertNext(tree -> {
                assertEquals(6, tree.size());
                assertTreeStructure(tree.values());
                assertEquals(target.getId(), tree.get(branch.getId()).getParentId());
                assertEquals(Integer.valueOf(4), tree.get(branch.getId()).getLevel());
                assertEquals(Integer.valueOf(5), tree.get(leaf.getId()).getLevel());
                assertEquals(branch.getName(), tree.get(branch.getId()).getName());
                assertEquals(Long.valueOf(17), tree.get(branch.getId()).getSortIndex());
                assertEquals(Long.valueOf(23), tree.get(leaf.getId()).getSortIndex());
                assertEquals(root.getPath(), tree.get(root.getId()).getPath());
                assertEquals(targetRoot.getPath(), tree.get(targetRoot.getId()).getPath());
                assertEquals(targetBranch.getPath(), tree.get(targetBranch.getId()).getPath());
                assertEquals(target.getPath(), tree.get(target.getId()).getPath());
            }).verifyComplete();
            scoped.queryIncludeChildren(List.of(root.getId())).count()
                .as(StepVerifier::create).expectNext(1L).verifyComplete();
            scoped.queryIncludeChildren(List.of(branch.getId())).collectList()
                .as(StepVerifier::create).assertNext(nodes ->
                    assertEquals(Set.of(branch.getId(), leaf.getId()), ids(nodes))).verifyComplete();
            scoped.deleteById(targetRoot.getId()).as(StepVerifier::create).expectNext(5).verifyComplete();
            scoped.deleteById(root.getId()).as(StepVerifier::create).expectNext(1).verifyComplete();
        }
    }

    @Test
    public void bothUpdateByIdMovesRollbackAfterDescendantModifyEventOrCallerFailure() {
        for (String overload : List.of("entity", "mono")) {
            for (String failure : List.of("modify", "caller")) {
                String group = id();
                TestTreeSortEntity target = node(group, null, null);
                TestTreeSortEntity root = node(group, null, null);
                TestTreeSortEntity branch = node(group, root.getId(), null);
                TestTreeSortEntity leaf = node(group, branch.getId(), null);
                if ("modify".equals(failure)) {
                    // 初始 save 正常完成；移动目标写入后，遗漏后代的真实 Modify 后事件拒绝事务。
                    leaf.setName("reject-dsl-" + id());
                }
                scoped.save(List.of(target, root, branch, leaf))
                    .as(StepVerifier::create).expectNextCount(1).verifyComplete();
                AtomicReference<Map<String, TestTreeSortEntity>> before = new AtomicReference<>();
                stored(scoped, group).as(StepVerifier::create).assertNext(before::set).verifyComplete();
                TestTreeSortEntity patch = new TestTreeSortEntity();
                patch.setParentId(target.getId());
                patch.setName("patched-" + id());
                Mono<Integer> move = "entity".equals(overload) ? scoped.updateById(branch.getId(), patch)
                    : scoped.updateById(branch.getId(), Mono.just(patch));
                Mono<?> operation = move;
                if ("caller".equals(failure)) {
                    operation = TransactionUtils.tryRunInTransaction(
                        move.doOnNext(total -> assertEquals(Integer.valueOf(2), total))
                            .then(Mono.error(new IllegalStateException("reject patch caller"))),
                        new DefaultTransactionDefinition());
                }
                operation.as(StepVerifier::create).expectErrorMatches(error ->
                    error instanceof IllegalStateException && error.getMessage().equals(
                        "modify".equals(failure) ? "reject modified tree" : "reject patch caller"))
                    .verify();
                stored(scoped, group).as(StepVerifier::create).assertNext(after ->
                    assertSnapshot(before.get(), after)).verifyComplete();
                scoped.getRepository().createDelete().where("groupId", group).execute()
                    .as(StepVerifier::create).expectNext(4).verifyComplete();
            }
        }
    }

    private static Mono<Map<String, TestTreeSortEntity>> stored(ScopedTreeService service, String group) {
        return service.createQuery().where("groupId", group).fetch().collectMap(TestTreeSortEntity::getId);
    }

    private static void assertSnapshot(Map<String, TestTreeSortEntity> before,
                                       Map<String, TestTreeSortEntity> after) {
        assertEquals(before.keySet(), after.keySet());
        for (TestTreeSortEntity expected : before.values()) {
            TestTreeSortEntity actual = after.get(expected.getId());
            assertEquals(expected.getParentId(), actual.getParentId());
            assertEquals(expected.getPath(), actual.getPath());
            assertEquals(expected.getLevel(), actual.getLevel());
            assertEquals(expected.getSortIndex(), actual.getSortIndex());
            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getGroupId(), actual.getGroupId());
        }
        assertTreeStructure(after.values());
    }

    private static TestTreeSortEntity deleteBatchNode(String group, String parent) {
        TestTreeSortEntity node = node(group, parent, null);
        node.setName("verify-delete-batch-" + node.getId());
        return node;
    }

    private static void assertTreeStructure(Collection<TestTreeSortEntity> nodes) {
        Map<String, TestTreeSortEntity> tree = nodes.stream()
            .collect(Collectors.toMap(TestTreeSortEntity::getId, node -> node));
        for (TestTreeSortEntity node : nodes) {
            if (node.getParentId() == null) {
                assertEquals(Integer.valueOf(1), node.getLevel());
            } else {
                TestTreeSortEntity parent = tree.get(node.getParentId());
                assertNotNull(parent);
                assertEquals(parent.getGroupId(), node.getGroupId());
                assertTrue(node.getPath().startsWith(parent.getPath() + "-"));
                assertEquals(Integer.valueOf(parent.getLevel() + 1), node.getLevel());
            }
        }
    }

    private static TestTreeSortEntity batchNode(String group, String parent) {
        TestTreeSortEntity node = node(group, parent, null);
        node.setName("verify-batch-" + node.getId());
        return node;
    }

    private static Set<String> ids(List<TestTreeSortEntity> nodes) {
        return nodes.stream().map(TestTreeSortEntity::getId).collect(Collectors.toSet());
    }

    private static String id() {
        return UUID.randomUUID().toString();
    }

    private static TestTreeSortEntity node(String group, String parent, String path) {
        TestTreeSortEntity node = new TestTreeSortEntity();
        node.setId(id());
        node.setName(node.getId());
        node.setGroupId(group);
        node.setParentId(parent);
        node.setPath(path);
        node.setLevel(parent == null ? 1 : 2);
        return node;
    }

    static class ScopedTreeService extends GenericReactiveCrudService<TestTreeSortEntity, String>
        implements ReactiveTreeSortEntityService<TestTreeSortEntity, String> {
        ScopedTreeService(ReactiveRepository<TestTreeSortEntity, String> repository) {
            super(repository);
        }

        @Override
        public IDGenerator<String> getIDGenerator() {
            return IDGenerator.MD5;
        }

        @Override
        public void setChildren(TestTreeSortEntity entity, List<TestTreeSortEntity> children) {
            entity.setChildren(children);
        }

        @Override
        public ReactiveQuery<TestTreeSortEntity> applyTreeScope(TestTreeSortEntity node,
                                                              ReactiveQuery<TestTreeSortEntity> query) {
            return query.and("groupId", node.getGroupId());
        }
    }

    static class BatchedTreeService extends ScopedTreeService {
        BatchedTreeService(ReactiveRepository<TestTreeSortEntity, String> repository) {
            super(repository);
        }

        @Override
        public int getBufferSize() {
            return 2;
        }
    }

    static class FailureListener {
        private final ReactiveRepository<TestTreeSortEntity, String> repository;
        private final AtomicInteger observedRoots = new AtomicInteger();
        private final Map<String, List<Integer>> deleteBatches = new ConcurrentHashMap<>();

        FailureListener(ReactiveRepository<TestTreeSortEntity, String> repository) {
            this.repository = repository;
        }

        @EventListener
        public void beforeCreate(EntityBeforeCreateEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                event.async(validateBatch(event.getEntity()));
            }
        }

        @EventListener
        public void beforeSave(EntityBeforeSaveEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                event.async(validateBatch(event.getEntity()));
            }
        }

        private Mono<Void> validateBatch(List<TestTreeSortEntity> input) {
            return Mono.defer(() -> {
                Map<String, TestTreeSortEntity> batch = input.stream()
                    .collect(Collectors.toMap(TestTreeSortEntity::getId, node -> node));
                return Flux.fromIterable(input).filter(this::isBatchNode)
                    .filter(node -> node.getParentId() != null).concatMap(node -> {
                        TestTreeSortEntity parent = batch.get(node.getParentId());
                        Mono<TestTreeSortEntity> parentState = parent == null
                            ? repository.findById(node.getParentId()).switchIfEmpty(Mono.error(
                                new IllegalStateException("parent batch has not been written")))
                            : Mono.just(parent);
                        return parentState.doOnNext(stored -> {
                            assertEquals(stored.getGroupId(), node.getGroupId());
                            assertTrue(node.getPath().startsWith(stored.getPath() + "-"));
                            assertEquals(Integer.valueOf(stored.getLevel() + 1), node.getLevel());
                            if (parent == null) {
                                // 此字段由前批真实异步 afterCreate/afterSave SQL 更新。
                                assertEquals("tree-event-complete", stored.getDefaultTest());
                            }
                        }).then();
                    }).then();
            });
        }

        private boolean isBatchNode(TestTreeSortEntity node) {
            return node.getName().startsWith("verify-batch-") || node.getName().startsWith("reject-batch-event-");
        }

        private Mono<Void> completeBatch(List<TestTreeSortEntity> input) {
            List<TestTreeSortEntity> nodes = input.stream().filter(this::isBatchNode).collect(Collectors.toList());
            if (nodes.stream().anyMatch(node -> node.getName().startsWith("reject-batch-event-"))) {
                return Mono.error(new IllegalStateException("reject later batch event"));
            }
            if (nodes.isEmpty()) {
                return Mono.empty();
            }
            return repository.createUpdate().set("defaultTest", "tree-event-complete")
                .in("id", nodes.stream().map(TestTreeSortEntity::getId).collect(Collectors.toList()))
                .execute().then();
        }

        @EventListener
        public void afterCreate(EntityCreatedEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                event.async(completeBatch(event.getEntity()));
            }
        }

        @EventListener
        public void afterModify(EntityModifyEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class
                && event.getAfter().stream().anyMatch(node -> node.getName().startsWith("reject-dsl-")
                    || (node.getParentId() == null && node.getName().startsWith("reject-root-modify-")))) {
                event.async(Mono.error(new IllegalStateException("reject modified tree")));
            }
        }

        @EventListener
        public void afterSave(EntitySavedEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() != TestTreeSortEntity.class) {
                return;
            }
            event.async(completeBatch(event.getEntity()));
            for (TestTreeSortEntity node : event.getEntity()) {
                if (node.getParentId() == null && node.getName().startsWith("reject-root-save-")) {
                    event.async(Mono.error(new IllegalStateException("reject saved root")));
                }
                if (node.getParentId() == null && node.getName().startsWith("verify-root-")) {
                    event.async(repository.findById(node.getId()).doOnNext(stored -> {
                        assertNull(stored.getParentId());
                        assertEquals(node.getPath(), stored.getPath());
                        assertEquals(Integer.valueOf(1), stored.getLevel());
                        observedRoots.incrementAndGet();
                    }).then());
                }
            }
        }

        @EventListener
        public void beforeDelete(EntityBeforeDeleteEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() != TestTreeSortEntity.class
                || event.getEntity().stream().noneMatch(node -> node.getName().startsWith("verify-delete-batch-")
                    || node.getName().startsWith("reject-delete-batch-later-"))) {
                return;
            }
            event.async(Mono.defer(() -> {
                List<TestTreeSortEntity> batch = event.getEntity();
                String group = batch.get(0).getGroupId();
                deleteBatches.computeIfAbsent(group, ignored -> new CopyOnWriteArrayList<>()).add(batch.size());
                assertTrue("delete batch exceeds configured buffer", batch.size() <= 2);
                List<String> batchIds = batch.stream().map(TestTreeSortEntity::getId).collect(Collectors.toList());
                // 在真实beforeDelete阶段查询，父节点的子级只能已被前批删除或属于当前批。
                return repository.createQuery().where("groupId", group).in("parentId", batchIds)
                    .notIn("id", batchIds).count().doOnNext(outsideChildren ->
                        assertEquals("parent deleted before an outside child", Integer.valueOf(0), outsideChildren)).then();
            }));
        }

        @EventListener
        public void afterDelete(EntityDeletedEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class
                && event.getEntity().stream().anyMatch(node -> node.getName().startsWith("reject-delete-"))) {
                event.async(Mono.error(new IllegalStateException("reject deleted tree")));
            }
        }
    }

    @TestConfiguration
    static class Configuration {
        @Bean
        ScopedTreeService scopedTreeService(ReactiveRepository<TestTreeSortEntity, String> repository) {
            return new ScopedTreeService(repository);
        }

        @Bean
        BatchedTreeService batchedTreeService(ReactiveRepository<TestTreeSortEntity, String> repository) {
            return new BatchedTreeService(repository);
        }

        @Bean
        FailureListener treeFailureListener(ReactiveRepository<TestTreeSortEntity, String> repository) {
            return new FailureListener(repository);
        }
    }
}
