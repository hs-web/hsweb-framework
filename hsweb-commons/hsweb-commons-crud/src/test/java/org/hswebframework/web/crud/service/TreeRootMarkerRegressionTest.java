package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.crud.TestApplication;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.crud.utils.TransactionUtils;
import org.hswebframework.web.id.IDGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * 根标记通过 parentId 表达，实体自身 ID 不改变合法父子关系。
 * 每个场景使用独立业务组、真实 R2DBC H2 和正式实体事件，期望来自声明的父关系。
 *
 * @see ReactiveTreeSortEntityService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApplication.class, TestTreeSortEntityService.class,
    TreeRootMarkerRegressionTest.EventConfiguration.class}, properties = {
    "spring.r2dbc.generate-unique-name=true",
    "spring.r2dbc.name=TreeRootMarkerRegressionTest"
})
public class TreeRootMarkerRegressionTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Autowired private TestTreeSortEntityService service;
    @Autowired private MarkerTreeService marked;
    @Autowired private TreeCrudTestSupport.SaveEvents events;

    @Test
    public void insertNullParentRootKeepsNestedChild() { verifyRootInsert(null); }

    @Test
    public void insertEmptyParentRootKeepsNestedChild() { verifyRootInsert(""); }

    @Test
    public void insertMinusOneParentRootKeepsNestedChild() { verifyRootInsert("-1"); }

    @Test
    public void sparseAndFullNullParentSaveKeepExistingBranchAndOmittedLeaf() {
        for (boolean sparse : List.of(false, true)) {
            String group = IDGenerator.RANDOM.generate();
            TestTreeSortEntity root = TreeCrudTestSupport.node(group, null, "root", 1L);
            TestTreeSortEntity branch = TreeCrudTestSupport.node(group, root.getId(), "branch", 101L);
            TestTreeSortEntity leaf = TreeCrudTestSupport.node(group, branch.getId(), "leaf", 10101L);
            service.insert(Flux.just(root, branch, leaf)).block(TIMEOUT);
            Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(stored(group));
            TestTreeSortEntity input = sparse ? new TestTreeSortEntity()
                : service.findById(branch.getId()).block(TIMEOUT);
            input.setId(branch.getId());
            input.setParentId(null);
            input.setName("renamed branch");
            // Save 仍验证 CreateGroup；合法必填业务列不因父级回归而改变。
            input.setDefaultTest(branch.getDefaultTest());
            events.watch(branch.getId(), Set.of(branch.getId()), null, null);

            assertEquals(1, service.save(input).block(TIMEOUT).getTotal());

            Map<String, String> parents = new LinkedHashMap<>();
            parents.put(root.getId(), null);
            parents.put(branch.getId(), root.getId());
            parents.put(leaf.getId(), branch.getId());
            List<TestTreeSortEntity> after = stored(group);
            TreeCrudTestSupport.assertPersistedTree(after, parents);
            Map<String, List<Object>> snapshots = TreeCrudTestSupport.snapshot(after);
            assertEquals(before.get(root.getId()), snapshots.get(root.getId()));
            assertEquals(before.get(leaf.getId()), snapshots.get(leaf.getId()));
            assertEquals(before.get(branch.getId()).subList(0, 3), snapshots.get(branch.getId()).subList(0, 3));
            assertEquals("renamed branch", snapshots.get(branch.getId()).get(3));
            assertEquals(before.get(branch.getId()), events.beforeDatabase);
            assertEquals(root.getId(), events.beforeInput.get(0));
            assertEquals(snapshots.get(branch.getId()), events.savedDatabase.get(branch.getId()));
            assertNoModify(Set.of(branch.getId(), leaf.getId()));
            assertNull(events.getBeforeSave(leaf.getId()));
            events.reset();
        }
    }

    @Test
    public void sparseSaveKeepsHistoricalNullRootAndOmittedChild() {
        String group = IDGenerator.RANDOM.generate();
        TestTreeSortEntity root = TreeCrudTestSupport.node(group, null, "historical root", 1L);
        TestTreeSortEntity child = TreeCrudTestSupport.node(group, root.getId(), "child", 101L);
        service.insert(Flux.just(root, child)).block(TIMEOUT);
        Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(stored(group));
        TestTreeSortEntity input = new TestTreeSortEntity();
        input.setId(root.getId());
        input.setName("renamed historical root");
        input.setDefaultTest(root.getDefaultTest());
        service.save(input).block(TIMEOUT);
        Map<String, List<Object>> after = TreeCrudTestSupport.snapshot(stored(group));
        assertNull(after.get(root.getId()).get(0));
        assertEquals(before.get(root.getId()).subList(0, 3), after.get(root.getId()).subList(0, 3));
        assertEquals(before.get(child.getId()), after.get(child.getId()));
    }

    @Test
    public void saveEmptyParentMovesExistingBranchAndOmittedLeafToRoot() { verifyRootMove(""); }

    @Test
    public void saveMinusOneParentMovesExistingBranchAndOmittedLeafToRoot() { verifyRootMove("-1"); }

    @Test
    public void customRootMarkerWorksForSaveAndPureUpdate() {
        verifyRootMove("custom-root", marked);
        String group = IDGenerator.RANDOM.generate();
        TestTreeSortEntity root = TreeCrudTestSupport.node(group, null, "root", 1L);
        TestTreeSortEntity branch = TreeCrudTestSupport.node(group, root.getId(), "branch", 101L);
        TestTreeSortEntity leaf = TreeCrudTestSupport.node(group, branch.getId(), "leaf", 10101L);
        marked.insert(Flux.just(root, branch, leaf)).block(TIMEOUT);
        TestTreeSortEntity patch = new TestTreeSortEntity();
        patch.setParentId("custom-root");
        events.reset();
        assertEquals(Integer.valueOf(2), marked.updateById(branch.getId(), patch).block(TIMEOUT));
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put(root.getId(), null);
        parents.put(branch.getId(), "custom-root");
        parents.put(leaf.getId(), branch.getId());
        TreeCrudTestSupport.assertPersistedTree(stored(group), parents, Set.of(root.getId(), branch.getId()));
        assertNotNull(events.getModified(branch.getId()));
        assertNull(events.getSaved(branch.getId()));
    }

    @Test
    public void nestedSaveParentWinsOverNullRootMarkerAndOldParent() {
        for (String parentInput : new String[]{null, "", "-1", "missing-parent"}) {
            String group = IDGenerator.RANDOM.generate();
            TestTreeSortEntity root = TreeCrudTestSupport.node(group, null, "old root", 1L);
            TestTreeSortEntity target = TreeCrudTestSupport.node(group, null, "new root", 2L);
            TestTreeSortEntity branch = TreeCrudTestSupport.node(group, root.getId(), "branch", 101L);
            TestTreeSortEntity leaf = TreeCrudTestSupport.node(group, branch.getId(), "leaf", 10101L);
            service.insert(Flux.just(root, target, branch, leaf)).block(TIMEOUT);
            TestTreeSortEntity childInput = service.findById(branch.getId()).block(TIMEOUT);
            childInput.setParentId(parentInput);
            target.setChildren(List.of(childInput));
            events.reset();
            service.save(target).block(TIMEOUT);
            Map<String, String> parents = new LinkedHashMap<>();
            parents.put(root.getId(), null);
            parents.put(target.getId(), null);
            parents.put(branch.getId(), target.getId());
            parents.put(leaf.getId(), branch.getId());
            TreeCrudTestSupport.assertPersistedTree(stored(group), parents);
            assertNoModify(Set.of(target.getId(), branch.getId(), leaf.getId()));
        }
    }

    @Test
    public void explicitRootSavedRejectionAndCallerFailureRollbackAllBatches() {
        for (String marker : List.of("", "-1")) {
            for (boolean rejectEvent : List.of(false, true)) {
                TreeCrudTestSupport.Forest forest = new TreeCrudTestSupport.Forest();
                service.insert(Flux.fromIterable(forest.childFirst())).block(TIMEOUT);
                Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(stored(forest.group));
                TestTreeSortEntity input = service.findById(forest.id("east-left")).block(TIMEOUT);
                input.setParentId(marker);
                input.setName("renamed moved branch");
                Set<String> moved = TreeCrudTestSupport.subtree(forest.parents, input.getId());
                TestTreeSortEntity caller = TreeCrudTestSupport.node(forest.group, null, "caller row", 999L);
                events.watch(input.getId(), moved, rejectEvent ? forest.id("deep-leaf") : null, caller.getId());
                try {
                    Mono<?> operation = service.insert(caller)
                        .then(service.getRepository().createUpdate().set("name", "caller changed root")
                            .where("id", forest.id("east")).execute())
                        .then(marked.save(input));
                    if (!rejectEvent) {
                        operation = operation.then(Mono.error(new IllegalStateException("reject caller root marker")));
                    }
                    TransactionUtils.tryRunInTransaction(operation, new DefaultTransactionDefinition())
                        .as(StepVerifier::create).expectErrorMatches(error -> error instanceof IllegalStateException
                            && (rejectEvent ? "reject saved root marker" : "reject caller root marker")
                                .equals(error.getMessage())).verify(TIMEOUT);
                    assertEquals(before, TreeCrudTestSupport.snapshot(stored(forest.group)));
                    assertEquals(before.get(input.getId()), events.beforeDatabase);
                    assertEquals(marker, events.beforeInput.get(0));
                    assertEquals("renamed moved branch", events.beforeInput.get(3));
                    assertEquals(marker, events.savedDatabase.get(input.getId()).get(0));
                    assertEquals("renamed moved branch", events.savedDatabase.get(input.getId()).get(3));
                    assertEquals(moved, events.savedDatabase.keySet());
                    assertTrue("the rejection must follow earlier SQL batches", events.savedBatches.size() >= 2);
                    assertTrue(events.savedBatches.stream().allMatch(batch -> batch.size() <= marked.getBufferSize()));
                    if (rejectEvent) {
                        assertEquals(TreeCrudTestSupport.snapshot(List.of(caller)).get(caller.getId()), events.callerDatabase);
                    }
                    assertNoModify(moved);
                } finally {
                    events.reset();
                }
            }
        }
    }

    @Test
    public void minusOneEntityIdMovesBetweenLegalParentsAsOrdinaryLeaf() {
        String group = IDGenerator.RANDOM.generate();
        TestTreeSortEntity oldRoot = TreeCrudTestSupport.node(group, null, "old root", 1L);
        TestTreeSortEntity newRoot = TreeCrudTestSupport.node(group, null, "new root", 2L);
        TestTreeSortEntity leaf = TreeCrudTestSupport.node(group, oldRoot.getId(), "ordinary leaf", 101L);
        leaf.setId("-1");
        List<String> ids = List.of(oldRoot.getId(), newRoot.getId(), leaf.getId());
        try {
            assertEquals(Integer.valueOf(3), service.insert(Flux.fromIterable(List.of(oldRoot, newRoot, leaf))).block(TIMEOUT));
            Map<String, String> parents = new LinkedHashMap<>();
            parents.put(oldRoot.getId(), null);
            parents.put(newRoot.getId(), null);
            parents.put(leaf.getId(), oldRoot.getId());
            List<TestTreeSortEntity> before = stored(group);
            TreeCrudTestSupport.assertPersistedTree(before, parents);
            Map<String, List<Object>> business = TreeCrudTestSupport.businessSnapshot(before);
            TestTreeSortEntity moving = service.findById(leaf.getId()).block(TIMEOUT);
            moving.setParentId(newRoot.getId());
            events.reset();

            Signal<SaveResult> result = service.save(moving).materialize().block(TIMEOUT);

            List<TestTreeSortEntity> after = stored(group);
            assertFalse("a legal parent move must succeed: " + result.getThrowable(), result.isOnError());
            parents.put(leaf.getId(), newRoot.getId());
            TreeCrudTestSupport.assertPersistedTree(after, parents);
            assertEquals(business, TreeCrudTestSupport.businessSnapshot(after));
            assertSavedSnapshots(after, Set.of(leaf.getId()));
        } finally {
            // 固定 ID 反例只清理明确输入，避免被测树路径影响后续独立场景。
            service.getRepository().createDelete().in("id", ids).execute().block(TIMEOUT);
        }
    }

    private void verifyRootInsert(String marker) {
        String group = IDGenerator.RANDOM.generate();
        TestTreeSortEntity root = TreeCrudTestSupport.node(group, marker, "root", 1L);
        TestTreeSortEntity child = TreeCrudTestSupport.node(group, null, "child", 101L);
        root.setChildren(List.of(child));
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put(root.getId(), marker);
        parents.put(child.getId(), root.getId());
        events.reset();

        Signal<Integer> result = service.insert(root).materialize().block(TIMEOUT);

        List<TestTreeSortEntity> after = stored(group);
        assertFalse("a declared root must insert: " + result.getThrowable(), result.isOnError());
        assertEquals(Integer.valueOf(2), result.get());
        TreeCrudTestSupport.assertPersistedTree(after, parents, Set.of(root.getId()));
        Map<String, List<Object>> snapshots = TreeCrudTestSupport.snapshot(after);
        for (String id : parents.keySet()) {
            assertEquals(snapshots.get(id), events.getBeforeCreate(id));
            assertEquals(snapshots.get(id), events.getCreated(id));
        }
    }

    private void verifyRootMove(String marker) {
        verifyRootMove(marker, service);
    }

    private void verifyRootMove(String marker, ReactiveTreeSortEntityService<TestTreeSortEntity, String> saving) {
        String group = IDGenerator.RANDOM.generate();
        TestTreeSortEntity root = TreeCrudTestSupport.node(group, null, "root", 1L);
        TestTreeSortEntity branch = TreeCrudTestSupport.node(group, root.getId(), "branch", 101L);
        TestTreeSortEntity leaf = TreeCrudTestSupport.node(group, branch.getId(), "leaf", 10101L);
        assertEquals(Integer.valueOf(3), service.insert(Flux.fromIterable(List.of(root, branch, leaf))).block(TIMEOUT));
        List<TestTreeSortEntity> before = stored(group);
        Map<String, List<Object>> business = TreeCrudTestSupport.businessSnapshot(before);
        Map<String, List<Object>> snapshot = TreeCrudTestSupport.snapshot(before);
        TestTreeSortEntity moving = service.findById(branch.getId()).block(TIMEOUT);
        assertNull(moving.getChildren());
        moving.setParentId(marker);
        moving.setName("renamed branch");
        business.get(branch.getId()).set(0, moving.getName());
        events.watch(branch.getId(), Set.of(branch.getId(), leaf.getId()), null, null);

        Signal<SaveResult> result = saving.save(moving).materialize().block(TIMEOUT);

        List<TestTreeSortEntity> after = stored(group);
        assertFalse("an existing branch must move to a declared root: " + result.getThrowable(), result.isOnError());
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put(root.getId(), null);
        parents.put(branch.getId(), marker);
        parents.put(leaf.getId(), branch.getId());
        TreeCrudTestSupport.assertPersistedTree(after, parents, Set.of(root.getId(), branch.getId()));
        assertEquals(business, TreeCrudTestSupport.businessSnapshot(after));
        assertEquals(snapshot.get(root.getId()), TreeCrudTestSupport.snapshot(after).get(root.getId()));
        assertEquals(2, result.get().getTotal());
        assertSavedSnapshots(after, Set.of(branch.getId(), leaf.getId()));
        assertEquals("BeforeSave must read the unchanged old database row", snapshot.get(branch.getId()), events.beforeDatabase);
        assertEquals(TreeCrudTestSupport.snapshot(after).get(branch.getId()), events.beforeInput);
        assertEquals(TreeCrudTestSupport.snapshot(after).get(branch.getId()), events.savedDatabase.get(branch.getId()));
        assertNoModify(Set.of(branch.getId(), leaf.getId()));
        assertEquals(Set.of(branch.getId(), leaf.getId()),
            TreeCrudTestSupport.ids(service.queryIncludeChildren(List.of(branch.getId())).collectList().block(TIMEOUT)));
    }

    private List<TestTreeSortEntity> stored(String group) {
        return service.query(TreeCrudTestSupport.query(group)).collectList().block(TIMEOUT);
    }

    private void assertSavedSnapshots(List<TestTreeSortEntity> after, Set<String> savedIds) {
        Map<String, List<Object>> snapshots = TreeCrudTestSupport.snapshot(after);
        for (String id : savedIds) {
            assertEquals(snapshots.get(id), events.getBeforeSave(id));
            assertEquals(snapshots.get(id), events.getSaved(id));
        }
    }

    private void assertNoModify(Set<String> ids) {
        for (String id : ids) {
            assertNull("Save must not publish BeforeModify", events.getBeforeModify(id));
            assertNull("Save must not publish Modify", events.getModified(id));
        }
    }

    /** 真实根标记扩展和小批次，沿用通用仓储与正式事件。 */
    static class MarkerTreeService extends GenericReactiveCrudService<TestTreeSortEntity, String>
        implements ReactiveTreeSortEntityService<TestTreeSortEntity, String> {
        MarkerTreeService(ReactiveRepository<TestTreeSortEntity, String> repository) { super(repository); }
        @Override public IDGenerator<String> getIDGenerator() { return IDGenerator.MD5; }
        @Override public void setChildren(TestTreeSortEntity node, List<TestTreeSortEntity> children) { node.setChildren(children); }
        @Override public int getBufferSize() { return 2; }
        @Override public boolean isRootNode(TestTreeSortEntity node) {
            return "custom-root".equals(node.getParentId()) || ReactiveTreeSortEntityService.super.isRootNode(node);
        }
    }

    @TestConfiguration
    public static class EventConfiguration {
        @Bean public TreeCrudTestSupport.SaveEvents rootMarkerEvents(ReactiveRepository<TestTreeSortEntity, String> repository) {
            return new TreeCrudTestSupport.SaveEvents(repository::findById);
        }

        @Bean public MarkerTreeService reactiveMarkerTreeService(ReactiveRepository<TestTreeSortEntity, String> repository) {
            return new MarkerTreeService(repository);
        }
    }
}
