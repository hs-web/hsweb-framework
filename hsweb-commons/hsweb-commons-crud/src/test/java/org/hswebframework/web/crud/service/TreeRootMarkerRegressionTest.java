package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.crud.TestApplication;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.id.IDGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;

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
    @Autowired private TreeCrudTestSupport.EntityEvents events;

    @Test
    public void insertNullParentRootKeepsNestedChild() { verifyRootInsert(null); }

    @Test
    public void insertEmptyParentRootKeepsNestedChild() { verifyRootInsert(""); }

    @Test
    public void insertMinusOneParentRootKeepsNestedChild() { verifyRootInsert("-1"); }

    @Test
    public void saveNullParentMovesExistingBranchAndOmittedLeafToRoot() { verifyRootMove(null); }

    @Test
    public void saveEmptyParentMovesExistingBranchAndOmittedLeafToRoot() { verifyRootMove(""); }

    @Test
    public void saveMinusOneParentMovesExistingBranchAndOmittedLeafToRoot() { verifyRootMove("-1"); }

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
        events.reset();

        Signal<SaveResult> result = service.save(moving).materialize().block(TIMEOUT);

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

    @TestConfiguration
    public static class EventConfiguration {
        @Bean public TreeCrudTestSupport.EntityEvents rootMarkerEvents() {
            return new TreeCrudTestSupport.EntityEvents();
        }
    }
}
