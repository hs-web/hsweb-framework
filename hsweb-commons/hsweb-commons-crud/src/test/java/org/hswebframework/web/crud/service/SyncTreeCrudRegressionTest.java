package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.crud.annotation.EnableEasyormRepository;
import org.hswebframework.web.crud.configuration.EasyormConfiguration;
import org.hswebframework.web.crud.configuration.EntityFactoryConfiguration;
import org.hswebframework.web.crud.configuration.JdbcSqlExecutorConfiguration;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.crud.events.EntityCreatedEvent;
import org.hswebframework.web.crud.events.EntityDeletedEvent;
import org.hswebframework.web.crud.events.EntitySavedEvent;
import org.hswebframework.web.crud.sql.DefaultJdbcExecutor;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.exception.ValidationException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/** 同步树入口使用真实 JDBC 仓储，验证移根的父 ID、路径和原事务保持一致。 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SyncTreeCrudRegressionTest.JdbcConfiguration.class)
@TestPropertySource(properties = {"easyorm.dialect=h2", "easyorm.default-schema=PUBLIC"})
public class SyncTreeCrudRegressionTest {

    @Autowired
    private JdbcTreeService service;

    @Autowired
    private SyncSqlExecutor executor;

    @Autowired
    private SavedEvents events;

    @Autowired
    @Qualifier("transactionManager")
    private PlatformTransactionManager transactionManager;

    @After
    public void resetFailure() {
        events.rejectedId = null;
        events.rejectedCreateId = null;
        events.rejectedDeleteId = null;
        events.createdBatches.clear();
        events.savedBatches.clear();
        events.deletedBatches.clear();
    }

    @Test
    public void updateByIdNamePatchKeepsParentAndPersistedSubtree() {
        assertTrue("the synchronous repository must execute JDBC SQL", executor instanceof DefaultJdbcExecutor);
        List<TestTreeSortEntity> tree = insertTree();
        TestTreeSortEntity branch = stored(tree.get(1).getId());
        TestTreeSortEntity leaf = stored(tree.get(2).getId());
        TestTreeSortEntity patch = new TestTreeSortEntity();
        patch.setName("renamed branch");
        patch.setDefaultTest(branch.getDefaultTest());

        // 同步部分 UPDATE 保留原有树关系，此入口不扩展为结构 patch。
        assertEquals(1, service.updateById(branch.getId(), patch));

        TestTreeSortEntity after = stored(branch.getId());
        assertEquals("renamed branch", after.getName());
        assertSameTreeState(branch, after);
        assertSameTreeState(leaf, stored(leaf.getId()));
        assertEquals(3, service.queryIncludeChildren(List.of(tree.get(0).getId())).size());
    }

    @Test
    public void listSaveMovesBranchAndOmittedLeafToRootInDatabase() {
        List<TestTreeSortEntity> tree = insertTree();
        TestTreeSortEntity root = stored(tree.get(0).getId());
        TestTreeSortEntity branch = stored(tree.get(1).getId());
        TestTreeSortEntity leaf = stored(tree.get(2).getId());
        assertNull(branch.getChildren());
        branch.setParentId(null);

        assertEquals(2, service.save(List.of(branch)).getTotal());

        TestTreeSortEntity moved = stored(branch.getId());
        TestTreeSortEntity movedLeaf = stored(leaf.getId());
        assertNull("a root path must be committed with a cleared parent ID", moved.getParentId());
        assertEquals(Integer.valueOf(1), moved.getLevel());
        assertFalse(moved.getPath().contains("-"));
        assertEquals(moved.getId(), movedLeaf.getParentId());
        assertTrue(movedLeaf.getPath().startsWith(moved.getPath() + "-"));
        assertEquals(Integer.valueOf(2), movedLeaf.getLevel());
        assertEquals(branch.getName(), moved.getName());
        assertEquals(leaf.getName(), movedLeaf.getName());
        assertEquals(branch.getSortIndex(), moved.getSortIndex());
        assertEquals(leaf.getSortIndex(), movedLeaf.getSortIndex());
        assertSameTreeState(root, stored(root.getId()));
        assertEquals(1, service.queryIncludeChildren(List.of(root.getId())).size());
        assertEquals(2, service.queryIncludeChildren(List.of(branch.getId())).size());
    }

    @Test
    public void listRootMoveRollsBackWithSavedEventOrCallerFailure() {
        for (String failure : List.of("event", "caller")) {
            List<TestTreeSortEntity> tree = insertTree();
            TestTreeSortEntity root = stored(tree.get(0).getId());
            TestTreeSortEntity branch = stored(tree.get(1).getId());
            TestTreeSortEntity leaf = stored(tree.get(2).getId());
            TestTreeSortEntity moved = stored(branch.getId());
            moved.setParentId(null);
            if ("event".equals(failure)) {
                events.rejectedId = branch.getId();
            }

            IllegalStateException rejected = assertThrows(IllegalStateException.class, () -> {
                if ("event".equals(failure)) {
                    service.save(List.of(moved));
                } else {
                    new TransactionTemplate(transactionManager).execute(status -> {
                        service.save(List.of(moved));
                        throw new IllegalStateException("reject caller transaction");
                    });
                }
            });
            assertTrue(rejected.getMessage().contains("reject"));
            events.rejectedId = null;

            assertSameTreeState(root, stored(root.getId()));
            assertSameTreeState(branch, stored(branch.getId()));
            assertSameTreeState(leaf, stored(leaf.getId()));
            assertEquals(3, service.queryIncludeChildren(List.of(root.getId())).size());
        }
    }

    @Test
    public void collectionInsertAndListSaveKeepChildFirstForestAndDeepOmittedDescendants() {
        TreeCrudTestSupport.Forest forest = new TreeCrudTestSupport.Forest();
        assertEquals(forest.nodes.size(), service.insert(forest.childFirst()));
        List<TestTreeSortEntity> before = storedGroup(forest.group);
        TreeCrudTestSupport.assertPersistedTree(before, forest.parents);
        Map<String, List<Object>> business = TreeCrudTestSupport.businessSnapshot(before);
        Set<String> movedIds = TreeCrudTestSupport.subtree(forest.parents, forest.id("east-left"));
        Map<String, List<Object>> untouched = TreeCrudTestSupport.snapshot(before.stream()
            .filter(node -> !movedIds.contains(node.getId())).collect(Collectors.toList()));
        TestTreeSortEntity moved = stored(forest.id("east-left"));
        moved.setParentId(forest.id("west-branch"));

        assertEquals(movedIds.size(), service.save(List.of(moved)).getTotal());

        Map<String, String> expected = new LinkedHashMap<>(forest.parents);
        expected.put(moved.getId(), forest.id("west-branch"));
        List<TestTreeSortEntity> after = storedGroup(forest.group);
        TreeCrudTestSupport.assertPersistedTree(after, expected);
        assertEquals(business, TreeCrudTestSupport.businessSnapshot(after));
        assertEquals(untouched, TreeCrudTestSupport.snapshot(after.stream()
            .filter(node -> !movedIds.contains(node.getId())).collect(Collectors.toList())));
        assertEquals(TreeCrudTestSupport.subtree(expected, forest.id("west")),
            TreeCrudTestSupport.ids(service.queryIncludeChildren(List.of(forest.id("west")))));
    }

    @Test
    public void smallBatchInsertAndListSaveRollbackEarlierSqlAndCallerWrites() {
        for (String operation : List.of("insert", "save")) {
            for (String failure : List.of("event", "caller")) {
                TreeCrudTestSupport.Forest forest = new TreeCrudTestSupport.Forest();
                TestTreeSortEntity original = TreeCrudTestSupport.node(forest.group, null, "original", 999L);
                service.insert(original);
                Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(storedGroup(forest.group));
                TestTreeSortEntity callerRow = node(null, "caller row");
                if ("event".equals(failure)) {
                    if ("insert".equals(operation)) {
                        events.rejectedCreateId = forest.id("deep-leaf");
                    } else {
                        events.rejectedId = forest.id("deep-leaf");
                    }
                }
                events.createdBatches.clear();
                events.savedBatches.clear();

                IllegalStateException rejected = assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactionManager)
                    .execute(status -> {
                        service.getRepository().save(callerRow);
                        service.createUpdate().set("name", "caller changed original")
                            .where("id", original.getId()).execute();
                        if ("insert".equals(operation)) {
                            assertEquals(forest.nodes.size(), service.insert(forest.childFirst()));
                        } else {
                            assertEquals(forest.nodes.size(), service.save(forest.childFirst()).getTotal());
                        }
                        if ("caller".equals(failure)) {
                            throw new IllegalStateException("reject caller tree transaction");
                        }
                        return 0;
                    }));

                assertEquals("event".equals(failure)
                    ? "insert".equals(operation) ? "reject later created tree event" : "reject saved tree event"
                    : "reject caller tree transaction", rejected.getMessage());
                assertEquals(before, TreeCrudTestSupport.snapshot(storedGroup(forest.group)));
                assertFalse(service.findById(callerRow.getId()).isPresent());
                List<Integer> batches = ("insert".equals(operation)
                    ? events.createdBatches : events.savedBatches).get(forest.group);
                assertNotNull(batches);
                assertTrue("failure must occur after earlier SQL batches", batches.size() >= 2);
                assertTrue(batches.stream().allMatch(size -> size <= service.getBufferSize()));
                resetFailure();
            }
        }
    }

    @Test
    public void listDeepMoveRollsBackOmittedDescendantsAfterLaterEventOrCallerFailure() {
        for (String failure : List.of("event", "caller")) {
            TreeCrudTestSupport.Forest forest = new TreeCrudTestSupport.Forest();
            assertEquals(forest.nodes.size(), service.insert(forest.childFirst()));
            Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(storedGroup(forest.group));
            TestTreeSortEntity callerRow = node(null, "caller row");
            TestTreeSortEntity moved = stored(forest.id("east-left"));
            moved.setParentId(forest.id("west-branch"));
            if ("event".equals(failure)) {
                events.rejectedId = forest.id("deep-leaf");
            }
            events.savedBatches.clear();

            IllegalStateException rejected = assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactionManager)
                .execute(status -> {
                    service.getRepository().save(callerRow);
                    service.createUpdate().set("name", "caller changed root")
                        .where("id", forest.id("east")).execute();
                    assertEquals(4, service.save(List.of(moved)).getTotal());
                    if ("caller".equals(failure)) {
                        throw new IllegalStateException("reject caller deep move");
                    }
                    return 0;
                }));

            assertEquals("event".equals(failure) ? "reject saved tree event" : "reject caller deep move",
                rejected.getMessage());
            assertEquals(before, TreeCrudTestSupport.snapshot(storedGroup(forest.group)));
            assertFalse(service.findById(callerRow.getId()).isPresent());
            assertEquals(2, events.savedBatches.get(forest.group).size());
            resetFailure();
        }
    }

    @Test
    public void collectionInsertAndListSaveRejectInvalidGraphsInOriginalTransaction() {
        for (String operation : List.of("insert-missing", "insert-cycle", "save-missing", "save-cycle")) {
            TreeCrudTestSupport.Forest forest = new TreeCrudTestSupport.Forest();
            assertEquals(forest.nodes.size(), service.insert(forest.childFirst()));
            Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(storedGroup(forest.group));
            TestTreeSortEntity callerRow = node(null, "caller row");
            List<TestTreeSortEntity> invalid;
            if (operation.startsWith("save")) {
                TestTreeSortEntity root = stored(forest.id("east"));
                root.setParentId(operation.endsWith("missing") ? IDGenerator.RANDOM.generate()
                    : forest.id("deep-leaf"));
                invalid = List.of(root);
            } else {
                TestTreeSortEntity first = TreeCrudTestSupport.node(forest.group, null, "invalid first", 1L);
                TestTreeSortEntity second = TreeCrudTestSupport.node(forest.group, first.getId(), "invalid second", 2L);
                first.setParentId(operation.endsWith("missing") ? IDGenerator.RANDOM.generate() : second.getId());
                invalid = List.of(second, first);
            }

            ValidationException rejected = assertThrows(ValidationException.class, () ->
                new TransactionTemplate(transactionManager).execute(status -> {
                    service.getRepository().save(callerRow);
                    service.createUpdate().set("name", "must roll back")
                        .where("id", forest.id("east")).execute();
                    return operation.startsWith("insert") ? service.insert(invalid) : service.save(invalid);
                }));

            assertTrue(rejected.getMessage().contains(operation.endsWith("missing")
                ? "tree_entity_parent_id_not_exist" : "tree_entity_cyclic_dependency"));
            assertEquals(before, TreeCrudTestSupport.snapshot(storedGroup(forest.group)));
            assertFalse(service.findById(callerRow.getId()).isPresent());
        }
    }

    @Test
    public void collectionDeleteHandlesOverlappingAndMissingSeedsAndKeepsOtherTree() {
        TreeCrudTestSupport.Forest forest = new TreeCrudTestSupport.Forest();
        assertEquals(forest.nodes.size(), service.insert(forest.childFirst()));
        Set<String> deletedIds = TreeCrudTestSupport.subtree(forest.parents, forest.id("east"));
        Set<String> survivingIds = TreeCrudTestSupport.subtree(forest.parents, forest.id("west"));
        Map<String, List<Object>> surviving = TreeCrudTestSupport.snapshot(service.findById(survivingIds));

        assertEquals(deletedIds.size(), service.deleteById(List.of(forest.id("east"),
            forest.id("east-left"), IDGenerator.RANDOM.generate())));

        List<TestTreeSortEntity> after = storedGroup(forest.group);
        TreeCrudTestSupport.assertPersistedTree(after, TreeCrudTestSupport.subset(forest.parents, survivingIds));
        assertEquals(surviving, TreeCrudTestSupport.snapshot(after));
        assertEquals(0, service.deleteById(List.of(IDGenerator.RANDOM.generate())));
        assertEquals(0, service.deleteById(List.of()));
    }

    @Test
    public void collectionDeleteRollsBackSqlAndCallerWritesWithRealEvents() {
        for (String failure : List.of("event", "caller")) {
            TreeCrudTestSupport.Forest forest = new TreeCrudTestSupport.Forest();
            assertEquals(forest.nodes.size(), service.insert(forest.childFirst()));
            Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(storedGroup(forest.group));
            TestTreeSortEntity callerRow = node(null, "caller row");
            if ("event".equals(failure)) {
                events.rejectedDeleteId = forest.id("east-left");
            }
            IllegalStateException rejected = assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactionManager)
                .execute(status -> {
                    service.getRepository().save(callerRow);
                    service.createUpdate().set("name", "caller changed survivor")
                        .where("id", forest.id("west")).execute();
                    assertEquals(7, service.deleteById(List.of(forest.id("east"),
                        forest.id("east-left"), IDGenerator.RANDOM.generate())));
                    if ("caller".equals(failure)) {
                        throw new IllegalStateException("reject caller deletion");
                    }
                    return 0;
                }));
            assertEquals("event".equals(failure) ? "reject deleted tree event" : "reject caller deletion",
                rejected.getMessage());
            assertEquals(before, TreeCrudTestSupport.snapshot(storedGroup(forest.group)));
            assertFalse(service.findById(callerRow.getId()).isPresent());
            assertTrue("real deletion event must run after SQL", events.deletedBatches.containsKey(forest.group));
            resetFailure();
        }
    }

    @Test
    public void queriesReturnCompleteForestFilteredRootsAndFullChildren() {
        TreeCrudTestSupport.Forest forest = new TreeCrudTestSupport.Forest();
        assertEquals(forest.nodes.size(), service.insert(forest.childFirst()));
        TreeCrudTestSupport.assertShape(service.queryResultToTree(TreeCrudTestSupport.query(forest.group)),
            forest.parents, forest.rootIds());
        String branch = forest.id("east-left");
        Set<String> subtree = TreeCrudTestSupport.subtree(forest.parents, branch);
        Map<String, String> expected = TreeCrudTestSupport.subset(forest.parents, subtree);
        QueryParamEntity filtered = TreeCrudTestSupport.query(forest.group);
        filtered.toQuery().in("id", subtree);
        TreeCrudTestSupport.assertShape(service.queryResultToTree(filtered), expected, Set.of(branch));
        TreeCrudTestSupport.assertShape(service.queryIncludeChildrenTree(QueryParamEntity.of("id", branch)),
            expected, Set.of(branch));
        assertTrue(service.queryResultToTree(QueryParamEntity.of("id", IDGenerator.RANDOM.generate())).isEmpty());
        assertTrue(service.queryIncludeChildrenTree(QueryParamEntity.of("id", IDGenerator.RANDOM.generate())).isEmpty());
    }

    @Test
    public void nestedInsertGeneratesDefaultSortAndExplicitReorderControlsQueryOrder() {
        String group = IDGenerator.RANDOM.generate();
        TestTreeSortEntity root = TreeCrudTestSupport.node(group, null, "sort root", null);
        TestTreeSortEntity left = TreeCrudTestSupport.node(group, null, "sort left", null);
        TestTreeSortEntity right = TreeCrudTestSupport.node(group, null, "sort right", null);
        TestTreeSortEntity leaf = TreeCrudTestSupport.node(group, null, "sort leaf", null);
        root.setChildren(List.of(left, right));
        left.setChildren(List.of(leaf));
        service.insert(root);
        assertEquals(Long.valueOf(1), stored(root.getId()).getSortIndex());
        assertEquals(Long.valueOf(101), stored(left.getId()).getSortIndex());
        assertEquals(Long.valueOf(102), stored(right.getId()).getSortIndex());
        assertEquals(Long.valueOf(10101), stored(leaf.getId()).getSortIndex());

        TestTreeSortEntity reorderedLeft = stored(left.getId());
        TestTreeSortEntity reorderedRight = stored(right.getId());
        reorderedLeft.setSortIndex(102L);
        reorderedRight.setSortIndex(101L);
        assertEquals(2, service.save(List.of(reorderedLeft, reorderedRight)).getTotal());
        QueryParamEntity ordered = TreeCrudTestSupport.query(group);
        ordered.toQuery().orderByAsc("sortIndex").orderByAsc("id");
        List<TestTreeSortEntity> roots = service.queryResultToTree(ordered);
        assertEquals(List.of(right.getId(), left.getId()), roots.get(0).getChildren().stream()
            .map(TestTreeSortEntity::getId).collect(Collectors.toList()));
        assertEquals(leaf.getId(), TreeCrudTestSupport.returnedNode(roots, left.getId()).getChildren().get(0).getId());
    }

    private List<TestTreeSortEntity> storedGroup(String group) {
        return service.query(TreeCrudTestSupport.query(group));
    }

    private List<TestTreeSortEntity> insertTree() {
        TestTreeSortEntity root = node(null, "root");
        TestTreeSortEntity branch = node(root.getId(), "branch");
        TestTreeSortEntity leaf = node(branch.getId(), "leaf");
        List<TestTreeSortEntity> tree = List.of(root, branch, leaf);
        assertEquals(3, service.insert(tree));
        return tree;
    }

    private TestTreeSortEntity stored(String id) {
        return service.findById(id).orElseThrow(AssertionError::new);
    }

    private static TestTreeSortEntity node(String parentId, String name) {
        TestTreeSortEntity node = new TestTreeSortEntity();
        node.setId(IDGenerator.RANDOM.generate());
        node.setParentId(parentId);
        node.setName(name);
        // 同步事件在 ORM 应用列默认值前校验 CreateGroup，夹具必须提供合法必填值。
        node.setDefaultTest("business-before");
        node.setSortIndex(17L);
        return node;
    }

    private static void assertSameTreeState(TestTreeSortEntity expected, TestTreeSortEntity actual) {
        assertEquals(expected.getParentId(), actual.getParentId());
        assertEquals(expected.getPath(), actual.getPath());
        assertEquals(expected.getLevel(), actual.getLevel());
        assertEquals(expected.getSortIndex(), actual.getSortIndex());
    }

    static class JdbcTreeService extends GenericTreeSupportCrudService<TestTreeSortEntity, String> {
        @Override
        public IDGenerator<String> getIDGenerator() {
            return IDGenerator.MD5;
        }

        @Override
        public void setChildren(TestTreeSortEntity entity, List<TestTreeSortEntity> children) {
            entity.setChildren(children);
        }

        @Override
        public int getBufferSize() {
            return 2;
        }
    }

    static class SavedEvents {
        private final SyncRepository<TestTreeSortEntity, String> repository;
        private volatile String rejectedId;
        private volatile String rejectedCreateId;
        private volatile String rejectedDeleteId;
        private final Map<String, List<Integer>> createdBatches = new LinkedHashMap<>();
        private final Map<String, List<Integer>> savedBatches = new LinkedHashMap<>();
        private final Map<String, List<Integer>> deletedBatches = new LinkedHashMap<>();

        SavedEvents(SyncRepository<TestTreeSortEntity, String> repository) {
            this.repository = repository;
        }

        private void record(Map<String, List<Integer>> batches, List<TestTreeSortEntity> nodes) {
            if (!nodes.isEmpty() && nodes.get(0).getGroupId() != null) {
                batches.computeIfAbsent(nodes.get(0).getGroupId(), ignored -> new ArrayList<>()).add(nodes.size());
            }
        }

        @EventListener
        public void created(EntityCreatedEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                record(createdBatches, event.getEntity());
                if (event.getEntity().stream().anyMatch(entity -> entity.getId().equals(rejectedCreateId))) {
                    event.async(Mono.error(new IllegalStateException("reject later created tree event")));
                }
            }
        }

        @EventListener
        public void saved(EntitySavedEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                record(savedBatches, event.getEntity());
                if (event.getEntity().stream().anyMatch(entity -> entity.getId().equals(rejectedId))) {
                    event.async(Mono.error(new IllegalStateException("reject saved tree event")));
                }
            }
        }

        @EventListener
        public void deleted(EntityDeletedEvent<TestTreeSortEntity> event) {
            if (event.getEntityType() == TestTreeSortEntity.class) {
                record(deletedBatches, event.getEntity());
                if (event.getEntity().stream().anyMatch(entity -> entity.getId().equals(rejectedDeleteId))) {
                    event.async(Mono.fromRunnable(() -> {
                        int remaining = repository.createQuery()
                            .in("id", TreeCrudTestSupport.ids(event.getEntity())).count();
                        assertEquals("deletion event must observe SQL in the original transaction", 0, remaining);
                        throw new IllegalStateException("reject deleted tree event");
                    }));
                }
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement(proxyTargetClass = true)
    @EnableEasyormRepository(value = "org.hswebframework.web.crud.entity.TestTreeSortEntity",
        reactive = false, nonReactive = true)
    @ImportAutoConfiguration({EasyormConfiguration.class, EntityFactoryConfiguration.class, JdbcSqlExecutorConfiguration.class})
    static class JdbcConfiguration {
        @Bean(destroyMethod = "shutdown")
        EmbeddedDatabase dataSource() {
            return new EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.H2).build();
        }

        @Bean
        JdbcTreeService jdbcTreeService() {
            return new JdbcTreeService();
        }

        @Bean
        SavedEvents savedEvents(SyncRepository<TestTreeSortEntity, String> repository) {
            return new SavedEvents(repository);
        }
    }
}
