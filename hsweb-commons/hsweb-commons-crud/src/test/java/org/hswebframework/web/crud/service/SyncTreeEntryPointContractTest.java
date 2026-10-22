package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.crud.events.EntityModifyEvent;
import org.hswebframework.web.id.IDGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * 同步树 Save 的静态类型入口共享完整关系，普通 updateById 保持纯 UPDATE。
 * 复用实际 JDBC/H2 配置和持久树断言，通过正式事件观察写入边界，不覆盖结构 patch。
 *
 * @see TreeSortEntityService
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SyncTreeCrudRegressionTest.JdbcConfiguration.class,
    SyncTreeEntryPointContractTest.EventConfiguration.class})
@TestPropertySource(properties = {"easyorm.dialect=h2", "easyorm.default-schema=PUBLIC"})
public class SyncTreeEntryPointContractTest {
    @Autowired private SyncTreeCrudRegressionTest.JdbcTreeService service;
    @Autowired private MarkerTreeService marked;
    @Autowired private TreeCrudTestSupport.SaveEvents events;
    @Autowired private RejectedModifyEvents rejectedModify;

    @Autowired
    @Qualifier("transactionManager")
    private PlatformTransactionManager transactionManager;

    @Test
    public void entitySaveMovesBranchAndRecalculatesOmittedLeaf() { verifySaveMove("entity"); }

    @Test
    public void collectionSaveMovesBranchAndRecalculatesOmittedLeaf() { verifySaveMove("collection"); }

    @Test
    public void listSaveMovesBranchAndRecalculatesOmittedLeaf() { verifySaveMove("list"); }

    @Test
    public void entitySparseAndFullNullParentSaveKeepExistingRelation() { verifyNullParentSave("entity"); }

    @Test
    public void collectionSparseAndFullNullParentSaveKeepExistingRelation() { verifyNullParentSave("collection"); }

    @Test
    public void listSparseAndFullNullParentSaveKeepExistingRelation() { verifyNullParentSave("list"); }

    @Test
    public void sparseSaveKeepsHistoricalNullRootAndOmittedChild() {
        String group = IDGenerator.RANDOM.generate();
        TestTreeSortEntity root = TreeCrudTestSupport.node(group, null, "historical root", 1L);
        TestTreeSortEntity child = TreeCrudTestSupport.node(group, null, "child", 101L);
        root.setChildren(List.of(child));
        service.save(root);
        Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(stored(group));
        TestTreeSortEntity input = new TestTreeSortEntity();
        input.setId(root.getId());
        input.setName("renamed historical root");
        input.setDefaultTest(root.getDefaultTest());
        service.save(input);
        Map<String, List<Object>> after = TreeCrudTestSupport.snapshot(stored(group));
        assertNull(after.get(root.getId()).get(0));
        assertEquals(before.get(root.getId()).subList(0, 3), after.get(root.getId()).subList(0, 3));
        assertEquals(before.get(child.getId()), after.get(child.getId()));
    }

    @Test
    public void insertMinusOneParentRootKeepsNestedChild() {
        String group = IDGenerator.RANDOM.generate();
        TestTreeSortEntity root = TreeCrudTestSupport.node(group, "-1", "root", 1L);
        TestTreeSortEntity child = TreeCrudTestSupport.node(group, null, "child", 101L);
        root.setChildren(List.of(child));
        events.reset();
        // -1 是声明根标记，夹具没有 ID=-1 的虚拟父记录。
        service.insert(root);
        List<TestTreeSortEntity> after = stored(group);
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put(root.getId(), "-1");
        parents.put(child.getId(), root.getId());
        TreeCrudTestSupport.assertPersistedTree(after, parents, Set.of(root.getId()));
        Map<String, List<Object>> snapshots = TreeCrudTestSupport.snapshot(after);
        for (String id : parents.keySet()) {
            assertEquals(snapshots.get(id), events.getBeforeCreate(id));
            assertEquals(snapshots.get(id), events.getCreated(id));
        }
    }

    @Test
    public void allSaveEntriesMoveToExplicitRootMarkersWithOmittedLeaf() {
        for (String entry : List.of("entity", "collection", "list")) {
            for (String marker : List.of("", "-1")) {
                verifyRootMove(service, entry, marker);
            }
        }
    }

    @Test
    public void customRootMarkerUsesExistingServiceExtension() {
        verifyRootMove(marked, "list", "custom-root");
    }

    @Test
    public void nestedSaveParentWinsOverNullRootMarkerAndOldParent() {
        for (String parentInput : new String[]{null, "", "-1", "missing-parent"}) {
            String group = IDGenerator.RANDOM.generate();
            TestTreeSortEntity root = TreeCrudTestSupport.node(group, null, "old root", 1L);
            TestTreeSortEntity target = TreeCrudTestSupport.node(group, null, "new root", 2L);
            TestTreeSortEntity branch = TreeCrudTestSupport.node(group, root.getId(), "branch", 101L);
            TestTreeSortEntity leaf = TreeCrudTestSupport.node(group, branch.getId(), "leaf", 10101L);
            service.insert(List.of(root, target, branch, leaf));
            TestTreeSortEntity childInput = service.findById(branch.getId()).orElseThrow(AssertionError::new);
            childInput.setParentId(parentInput);
            target.setChildren(List.of(childInput));
            events.reset();
            service.save(target);
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
                service.insert(forest.childFirst());
                Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(stored(forest.group));
                TestTreeSortEntity input = service.findById(forest.id("east-left")).orElseThrow(AssertionError::new);
                input.setParentId(marker);
                input.setName("renamed moved branch");
                Set<String> moved = TreeCrudTestSupport.subtree(forest.parents, input.getId());
                TestTreeSortEntity caller = TreeCrudTestSupport.node(forest.group, null, "caller row", 999L);
                events.watch(input.getId(), moved, rejectEvent ? forest.id("deep-leaf") : null, caller.getId());
                try {
                    IllegalStateException rejection = assertThrows(IllegalStateException.class,
                        () -> new TransactionTemplate(transactionManager).execute(status -> {
                            service.insert(caller);
                            service.getRepository().createUpdate().set("name", "caller changed root")
                                .where("id", forest.id("east")).execute();
                            service.save(input);
                            if (!rejectEvent) {
                                throw new IllegalStateException("reject caller root marker");
                            }
                            return 0;
                        }));
                    assertEquals(rejectEvent ? "reject saved root marker" : "reject caller root marker", rejection.getMessage());
                    assertEquals(before, TreeCrudTestSupport.snapshot(stored(forest.group)));
                    assertEquals(before.get(input.getId()), events.beforeDatabase);
                    assertEquals(marker, events.beforeInput.get(0));
                    assertEquals("renamed moved branch", events.beforeInput.get(3));
                    assertEquals(marker, events.savedDatabase.get(input.getId()).get(0));
                    assertEquals("renamed moved branch", events.savedDatabase.get(input.getId()).get(3));
                    assertEquals(moved, events.savedDatabase.keySet());
                    assertTrue("the rejection must follow earlier SQL batches", events.savedBatches.size() >= 2);
                    assertTrue(events.savedBatches.stream().allMatch(batch -> batch.size() <= service.getBufferSize()));
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

    private void verifyRootMove(TreeSortEntityService<TestTreeSortEntity, String> saving, String entry, String marker) {
        String group = IDGenerator.RANDOM.generate();
        TestTreeSortEntity root = TreeCrudTestSupport.node(group, null, "root", 1L);
        TestTreeSortEntity branch = TreeCrudTestSupport.node(group, root.getId(), "branch", 101L);
        TestTreeSortEntity leaf = TreeCrudTestSupport.node(group, branch.getId(), "leaf", 10101L);
        assertEquals(3, service.insert(List.of(root, branch, leaf)));
        List<TestTreeSortEntity> before = stored(group);
        Map<String, List<Object>> business = TreeCrudTestSupport.businessSnapshot(before);
        TestTreeSortEntity moving = service.findById(branch.getId()).orElseThrow(AssertionError::new);
        assertNull(moving.getChildren());
        moving.setParentId(marker);
        moving.setName("renamed branch");
        business.get(branch.getId()).set(0, moving.getName());
        events.watch(branch.getId(), Set.of(branch.getId(), leaf.getId()), null, null);

        SaveResult result = save(saving, entry, moving);

        List<TestTreeSortEntity> after = stored(group);
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put(root.getId(), null);
        parents.put(branch.getId(), marker);
        parents.put(leaf.getId(), branch.getId());
        TreeCrudTestSupport.assertPersistedTree(after, parents, Set.of(root.getId(), branch.getId()));
        assertEquals(business, TreeCrudTestSupport.businessSnapshot(after));
        assertEquals(2, result.getTotal());
        assertSavedSnapshots(after, Set.of(branch.getId(), leaf.getId()));
        assertEquals("BeforeSave must read the unchanged old database row",
            TreeCrudTestSupport.snapshot(before).get(branch.getId()), events.beforeDatabase);
        assertEquals(TreeCrudTestSupport.snapshot(after).get(branch.getId()), events.beforeInput);
        assertEquals(TreeCrudTestSupport.snapshot(after).get(branch.getId()), events.savedDatabase.get(branch.getId()));
        assertNoModify(Set.of(branch.getId(), leaf.getId()));
    }

    private void verifyNullParentSave(String entry) {
        for (boolean sparse : List.of(false, true)) {
            String group = IDGenerator.RANDOM.generate();
            TestTreeSortEntity root = TreeCrudTestSupport.node(group, null, "root", 1L);
            TestTreeSortEntity branch = TreeCrudTestSupport.node(group, root.getId(), "branch", 101L);
            TestTreeSortEntity leaf = TreeCrudTestSupport.node(group, branch.getId(), "leaf", 10101L);
            service.insert(List.of(root, branch, leaf));
            Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(stored(group));
            TestTreeSortEntity input = sparse ? new TestTreeSortEntity()
                : service.findById(branch.getId()).orElseThrow(AssertionError::new);
            input.setId(branch.getId());
            input.setParentId(null);
            input.setName("renamed branch");
            // Save 仍验证 CreateGroup；合法必填业务列不因父级回归而改变。
            input.setDefaultTest(branch.getDefaultTest());
            events.watch(branch.getId(), Set.of(branch.getId()), null, null);

            assertEquals(1, save(service, entry, input).getTotal());

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

    private SaveResult save(TreeSortEntityService<TestTreeSortEntity, String> saving, String entry, TestTreeSortEntity input) {
        switch (entry) {
            case "entity": return saving.save(input);
            case "collection":
                Collection<TestTreeSortEntity> collection = List.of(input);
                return saving.save(collection);
            case "list": return saving.save(List.of(input));
            default: throw new AssertionError(entry);
        }
    }

    @Test
    public void missingUpdateTargetReturnsZeroWithoutCreatingRow() {
        String group = IDGenerator.RANDOM.generate();
        String missingId = IDGenerator.RANDOM.generate();
        TestTreeSortEntity patch = TreeCrudTestSupport.node(group, null, "missing target", 1L);
        events.reset();

        int result = service.updateById(missingId, patch);

        List<TestTreeSortEntity> after = stored(group);
        assertTrue("updateById must not create its missing target", service.findById(missingId).isEmpty());
        assertEquals(0, result);
        assertTrue(after.isEmpty());
        assertNull(events.getBeforeSave(missingId));
        assertNull(events.getSaved(missingId));
    }

    @Test
    public void existingNamePatchUsesCompleteModifyWithoutSave() {
        String group = IDGenerator.RANDOM.generate();
        TestTreeSortEntity root = TreeCrudTestSupport.node(group, null, "root", 1L);
        TestTreeSortEntity branch = TreeCrudTestSupport.node(group, root.getId(), "branch", 101L);
        TestTreeSortEntity leaf = TreeCrudTestSupport.node(group, branch.getId(), "leaf", 10101L);
        assertEquals(3, service.insert(List.of(root, branch, leaf)));
        Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(stored(group));
        TestTreeSortEntity patch = new TestTreeSortEntity();
        patch.setName("renamed branch");
        patch.setDefaultTest(branch.getDefaultTest());
        // 合法必填业务值不变，遗漏 groupId 与树字段保持稀疏更新，不扩展为结构 patch。
        events.reset();

        int result = service.updateById(branch.getId(), patch);

        List<TestTreeSortEntity> after = stored(group);
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put(root.getId(), null);
        parents.put(branch.getId(), root.getId());
        parents.put(leaf.getId(), branch.getId());
        TreeCrudTestSupport.assertPersistedTree(after, parents);
        Map<String, List<Object>> snapshots = TreeCrudTestSupport.snapshot(after);
        assertEquals(before.get(root.getId()), snapshots.get(root.getId()));
        assertEquals(before.get(leaf.getId()), snapshots.get(leaf.getId()));
        TestTreeSortEntity updated = service.findById(branch.getId()).orElseThrow(AssertionError::new);
        assertEquals("renamed branch", updated.getName());
        assertEquals(branch.getDefaultTest(), updated.getDefaultTest());
        assertEquals(before.get(branch.getId()).get(1), updated.getPath());
        assertEquals(1, result);
        assertEquals(snapshots.get(branch.getId()), events.getBeforeModify(branch.getId()));
        assertEquals(snapshots.get(branch.getId()), events.getModified(branch.getId()));
        assertNull(events.getBeforeSave(branch.getId()));
        assertNull(events.getSaved(branch.getId()));
    }

    @Test
    public void modifiedEventRejectionRollsBackTargetAndCallerTransaction() {
        String group = IDGenerator.RANDOM.generate();
        TestTreeSortEntity root = TreeCrudTestSupport.node(group, null, "root", 1L);
        TestTreeSortEntity branch = TreeCrudTestSupport.node(group, root.getId(), "branch", 101L);
        TestTreeSortEntity leaf = TreeCrudTestSupport.node(group, branch.getId(), "leaf", 10101L);
        assertEquals(3, service.insert(List.of(root, branch, leaf)));
        Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(stored(group));
        TestTreeSortEntity callerRow = TreeCrudTestSupport.node(group, null, "caller row", 2L);
        TestTreeSortEntity patch = new TestTreeSortEntity();
        patch.setName("rejected branch name");
        patch.setDefaultTest(branch.getDefaultTest());
        List<Object> expectedTarget = new ArrayList<>(before.get(branch.getId()));
        expectedTarget.set(3, patch.getName());
        events.reset();
        rejectedModify.arm(branch.getId(), callerRow.getId());

        try {
            IllegalStateException rejection = assertThrows(IllegalStateException.class,
                () -> new TransactionTemplate(transactionManager).execute(status -> {
                    service.insert(callerRow);
                    return service.updateById(branch.getId(), patch);
                }));

            assertEquals("reject modified tree event", rejection.getMessage());
            assertEquals(before.get(branch.getId()), rejectedModify.eventBefore);
            assertEquals(expectedTarget, rejectedModify.eventAfter);
            assertEquals("the rejecting Modify event must observe updated JDBC SQL",
                expectedTarget, rejectedModify.persistedTarget);
            assertEquals("the event must observe the caller write in the original transaction",
                TreeCrudTestSupport.snapshot(List.of(callerRow)).get(callerRow.getId()),
                rejectedModify.persistedCaller);
            assertEquals(expectedTarget, events.getBeforeModify(branch.getId()));
            assertEquals(expectedTarget, events.getModified(branch.getId()));
            assertNull(events.getBeforeSave(branch.getId()));
            assertNull(events.getSaved(branch.getId()));
            assertEquals(before, TreeCrudTestSupport.snapshot(stored(group)));
            assertTrue(service.findById(callerRow.getId()).isEmpty());
        } finally {
            rejectedModify.disarm();
        }
    }

    private void verifySaveMove(String entryPoint) {
        String group = IDGenerator.RANDOM.generate();
        TestTreeSortEntity oldRoot = TreeCrudTestSupport.node(group, null, "old root", 1L);
        TestTreeSortEntity newRoot = TreeCrudTestSupport.node(group, null, "new root", 2L);
        TestTreeSortEntity branch = TreeCrudTestSupport.node(group, oldRoot.getId(), "branch", 101L);
        TestTreeSortEntity leaf = TreeCrudTestSupport.node(group, branch.getId(), "leaf", 10101L);
        assertEquals(4, service.insert(List.of(oldRoot, newRoot, branch, leaf)));
        List<TestTreeSortEntity> before = stored(group);
        Map<String, List<Object>> business = TreeCrudTestSupport.businessSnapshot(before);
        Map<String, List<Object>> beforeSnapshots = TreeCrudTestSupport.snapshot(before);
        TestTreeSortEntity moving = service.findById(branch.getId()).orElseThrow(AssertionError::new);
        assertNull(moving.getChildren());
        moving.setParentId(newRoot.getId());
        events.reset();

        SaveResult result = save(service, entryPoint, moving);

        List<TestTreeSortEntity> after = stored(group);
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put(oldRoot.getId(), null);
        parents.put(newRoot.getId(), null);
        parents.put(branch.getId(), newRoot.getId());
        parents.put(leaf.getId(), branch.getId());
        TreeCrudTestSupport.assertPersistedTree(after, parents);
        assertEquals(business, TreeCrudTestSupport.businessSnapshot(after));
        Map<String, List<Object>> afterSnapshots = TreeCrudTestSupport.snapshot(after);
        assertEquals(beforeSnapshots.get(oldRoot.getId()), afterSnapshots.get(oldRoot.getId()));
        assertEquals(beforeSnapshots.get(newRoot.getId()), afterSnapshots.get(newRoot.getId()));
        assertEquals(2, result.getTotal());
        assertSavedSnapshots(after, Set.of(branch.getId(), leaf.getId()));
        assertEquals(Set.of(newRoot.getId(), branch.getId(), leaf.getId()),
            TreeCrudTestSupport.ids(service.queryIncludeChildren(List.of(newRoot.getId()))));
    }

    private List<TestTreeSortEntity> stored(String group) {
        return service.query(TreeCrudTestSupport.query(group));
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

    /** 自定义根标记仍使用同一 JDBC 保存入口和正式事件。 */
    static class MarkerTreeService extends GenericTreeSupportCrudService<TestTreeSortEntity, String> {
        @Override public IDGenerator<String> getIDGenerator() { return IDGenerator.MD5; }
        @Override public void setChildren(TestTreeSortEntity node, List<TestTreeSortEntity> children) { node.setChildren(children); }
        @Override public boolean isRootNode(TestTreeSortEntity node) {
            return "custom-root".equals(node.getParentId()) || super.isRootNode(node);
        }
    }

    /** 在正式 Modify after 中读取原事务 SQL，拒绝只能由该实体事件触发。 */
    static class RejectedModifyEvents {
        private final SyncRepository<TestTreeSortEntity, String> repository;
        private String targetId;
        private String callerId;
        private List<Object> eventBefore;
        private List<Object> eventAfter;
        private List<Object> persistedTarget;
        private List<Object> persistedCaller;

        RejectedModifyEvents(SyncRepository<TestTreeSortEntity, String> repository) {
            this.repository = repository;
        }

        void arm(String targetId, String callerId) {
            this.targetId = targetId;
            this.callerId = callerId;
            eventBefore = null;
            eventAfter = null;
            persistedTarget = null;
            persistedCaller = null;
        }

        void disarm() {
            targetId = null;
            callerId = null;
        }

        @EventListener
        public void modified(EntityModifyEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class && targetId != null
                && event.getAfter().stream().anyMatch(node -> targetId.equals(node.getId()))) {
                event.async(Mono.fromRunnable(() -> {
                    eventBefore = TreeCrudTestSupport.snapshot(event.getBefore()).get(targetId);
                    eventAfter = TreeCrudTestSupport.snapshot(event.getAfter()).get(targetId);
                    persistedTarget = TreeCrudTestSupport.snapshot(List.of(repository.findById(targetId)
                        .orElseThrow(AssertionError::new))).get(targetId);
                    persistedCaller = TreeCrudTestSupport.snapshot(List.of(repository.findById(callerId)
                        .orElseThrow(AssertionError::new))).get(callerId);
                    throw new IllegalStateException("reject modified tree event");
                }));
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class EventConfiguration {
        @Bean TreeCrudTestSupport.SaveEvents syncEntryPointEvents(SyncRepository<TestTreeSortEntity, String> repository) {
            return new TreeCrudTestSupport.SaveEvents(id -> Mono.fromSupplier(() ->
                repository.findById(id).orElseThrow(AssertionError::new)));
        }

        @Bean MarkerTreeService jdbcMarkerTreeService() {
            return new MarkerTreeService();
        }

        @Bean RejectedModifyEvents syncRejectedModifyEvents(SyncRepository<TestTreeSortEntity, String> repository) {
            return new RejectedModifyEvents(repository);
        }
    }
}
