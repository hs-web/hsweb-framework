package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveQuery;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.crud.TestApplication;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.crud.events.EntityDeletedEvent;
import org.hswebframework.web.crud.utils.TransactionUtils;
import org.hswebframework.web.exception.ValidationException;
import org.hswebframework.web.id.IDGenerator;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.reactive.TransactionSynchronization;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/** 真实 R2DBC 覆盖非法图、完整查询树、排序与多批次取消；保持公开入口已有契约。 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApplication.class, ReactiveTreeCrudCoverageTest.EventConfiguration.class}, properties = {
    "spring.r2dbc.generate-unique-name=true",
    "spring.r2dbc.name=ReactiveTreeCrudCoverageTest"
})
public class ReactiveTreeCrudCoverageTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    private CoverageTreeService service;

    @Autowired
    private DeleteEvents events;

    @After
    public void resetEvents() {
        events.cancellation = null;
    }

    @Test
    public void monoPatchRejectsIllegalGraphsAndRollsBackCallerWrites() {
        verifyIllegalGraphs(false);
    }

    @Test
    public void entityPatchRejectsIllegalGraphsAndRollsBackCallerWrites() {
        verifyIllegalGraphs(true);
    }

    private void verifyIllegalGraphs(boolean entityOverload) {
        for (String scenario : List.of("missing", "self", "descendant", "nested-ancestor")) {
            TreeCrudTestSupport.Forest forest = insertForest();
            Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(storedGroup(forest.group));
            String targetId = forest.id("east-left");
            TestTreeSortEntity patch = new TestTreeSortEntity();
            patch.setName("target changed");
            if ("nested-ancestor".equals(scenario)) {
                TestTreeSortEntity ancestor = new TestTreeSortEntity();
                ancestor.setId(forest.id("east"));
                ancestor.setName("ancestor changed");
                patch.setChildren(List.of(ancestor));
            } else {
                patch.setParentId("missing".equals(scenario) ? IDGenerator.RANDOM.generate()
                    : "self".equals(scenario) ? targetId : forest.id("deep-leaf"));
            }
            TestTreeSortEntity callerRow = TreeCrudTestSupport.node(IDGenerator.RANDOM.generate(),
                null, "caller row", 1L);
            Mono<Integer> invalid = update(targetId, patch, entityOverload);
            Mono<Integer> caller = service.getRepository().save(callerRow)
                .then(service.getRepository().createUpdate().set("name", "must roll back")
                    .where("id", forest.id("west")).execute())
                .then(invalid);

            TransactionUtils.tryRunInTransaction(caller, new DefaultTransactionDefinition())
                .as(StepVerifier::create).expectErrorMatches(error -> error instanceof ValidationException
                    && error.getMessage().contains("missing".equals(scenario)
                        ? "tree_entity_parent_id_not_exist" : "tree_entity_cyclic_dependency"))
                .verify(TIMEOUT);

            assertEquals(before, TreeCrudTestSupport.snapshot(storedGroup(forest.group)));
            service.findById(callerRow.getId()).as(StepVerifier::create).expectComplete().verify(TIMEOUT);
        }
    }

    @Test
    public void queriesReturnCompleteForestFilteredRootsAndFullChildren() {
        TreeCrudTestSupport.Forest forest = insertForest();
        service.queryResultToTree(Mono.just(TreeCrudTestSupport.query(forest.group)))
            .as(StepVerifier::create).assertNext(roots ->
                TreeCrudTestSupport.assertShape(roots, forest.parents, forest.rootIds()))
            .expectComplete().verify(TIMEOUT);
        String branch = forest.id("east-left");
        Set<String> subtree = TreeCrudTestSupport.subtree(forest.parents, branch);
        Map<String, String> expected = TreeCrudTestSupport.subset(forest.parents, subtree);
        QueryParamEntity filtered = TreeCrudTestSupport.query(forest.group);
        filtered.toQuery().in("id", subtree);
        service.queryResultToTree(filtered).as(StepVerifier::create).assertNext(roots ->
            TreeCrudTestSupport.assertShape(roots, expected, Set.of(branch))).expectComplete().verify(TIMEOUT);
        service.queryIncludeChildrenTree(QueryParamEntity.of("id", branch)).as(StepVerifier::create)
            .assertNext(roots -> TreeCrudTestSupport.assertShape(roots, expected, Set.of(branch)))
            .expectComplete().verify(TIMEOUT);
        service.queryResultToTree(QueryParamEntity.of("id", IDGenerator.RANDOM.generate()))
            .as(StepVerifier::create).assertNext(roots -> assertTrue(roots.isEmpty()))
            .expectComplete().verify(TIMEOUT);
        service.queryIncludeChildrenTree(QueryParamEntity.of("id", IDGenerator.RANDOM.generate()))
            .as(StepVerifier::create).assertNext(roots -> assertTrue(roots.isEmpty()))
            .expectComplete().verify(TIMEOUT);
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
        service.insert(root).as(StepVerifier::create).expectNext(4).expectComplete().verify(TIMEOUT);
        Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(storedGroup(group));
        Map<String, TestTreeSortEntity> stored = storedGroup(group).stream()
            .collect(Collectors.toMap(TestTreeSortEntity::getId, node -> node));
        assertEquals(Long.valueOf(1), stored.get(root.getId()).getSortIndex());
        assertEquals(Long.valueOf(101), stored.get(left.getId()).getSortIndex());
        assertEquals(Long.valueOf(102), stored.get(right.getId()).getSortIndex());
        assertEquals(Long.valueOf(10101), stored.get(leaf.getId()).getSortIndex());
        TestTreeSortEntity leftPatch = new TestTreeSortEntity();
        leftPatch.setSortIndex(102L);
        TestTreeSortEntity rightPatch = new TestTreeSortEntity();
        rightPatch.setSortIndex(101L);
        service.updateById(left.getId(), leftPatch).then(service.updateById(right.getId(), Mono.just(rightPatch)))
            .as(StepVerifier::create).expectNext(1).expectComplete().verify(TIMEOUT);
        before.get(left.getId()).set(5, 102L);
        before.get(right.getId()).set(5, 101L);
        assertEquals(before, TreeCrudTestSupport.snapshot(storedGroup(group)));

        QueryParamEntity ordered = TreeCrudTestSupport.query(group);
        ordered.toQuery().orderByAsc("sortIndex").orderByAsc("id");
        service.queryResultToTree(ordered).as(StepVerifier::create).assertNext(roots -> {
            assertEquals(List.of(right.getId(), left.getId()), roots.get(0).getChildren().stream()
                .map(TestTreeSortEntity::getId).collect(Collectors.toList()));
            assertEquals(leaf.getId(), TreeCrudTestSupport.returnedNode(roots, left.getId()).getChildren().get(0).getId());
        }).expectComplete().verify(TIMEOUT);
    }

    @Test
    public void monoNewChildrenUsePersistedParentSort() {
        verifyNewChildSort(false);
    }

    @Test
    public void entityNewChildrenUsePersistedParentSort() {
        verifyNewChildSort(true);
    }

    private void verifyNewChildSort(boolean entityOverload) {
        String group = IDGenerator.RANDOM.generate();
        TestTreeSortEntity root = TreeCrudTestSupport.node(group, null, "parent before", 37L);
        service.insert(root).as(StepVerifier::create).expectNext(1).expectComplete().verify(TIMEOUT);
        TestTreeSortEntity first = TreeCrudTestSupport.node(group, null, "first child", null);
        TestTreeSortEntity second = TreeCrudTestSupport.node(group, null, "second child", null);
        TestTreeSortEntity patch = new TestTreeSortEntity();
        patch.setName("parent after");
        patch.setChildren(List.of(first, second));
        update(root.getId(), patch, entityOverload).as(StepVerifier::create)
            .expectNext(3).expectComplete().verify(TIMEOUT);
        Map<String, TestTreeSortEntity> actual = storedGroup(group).stream()
            .collect(Collectors.toMap(TestTreeSortEntity::getId, node -> node));
        assertEquals(Set.of(root.getId(), first.getId(), second.getId()), actual.keySet());
        assertEquals(Long.valueOf(37), actual.get(root.getId()).getSortIndex());
        assertEquals("parent after", actual.get(root.getId()).getName());
        assertEquals(root.getPath(), actual.get(root.getId()).getPath());
        assertEquals(root.getDefaultTest(), actual.get(root.getId()).getDefaultTest());
        assertEquals(Long.valueOf(3701), actual.get(first.getId()).getSortIndex());
        assertEquals(Long.valueOf(3702), actual.get(second.getId()).getSortIndex());
        for (TestTreeSortEntity child : List.of(first, second)) {
            TestTreeSortEntity persisted = actual.get(child.getId());
            assertEquals(root.getId(), persisted.getParentId());
            assertEquals(Integer.valueOf(2), persisted.getLevel());
            assertTrue(persisted.getPath().startsWith(root.getPath() + "-"));
            assertEquals(child.getDefaultTest(), persisted.getDefaultTest());
        }
    }

    @Test
    public void laterDeleteBatchCancellationRollsBackWholeTreeAndCallerWrites() {
        TreeCrudTestSupport.Forest forest = insertForest();
        Map<String, List<Object>> before = TreeCrudTestSupport.snapshot(storedGroup(forest.group));
        DeleteCancellation cancellation = new DeleteCancellation(forest.group,
            TreeCrudTestSupport.subtree(forest.parents, forest.id("east")));
        events.cancellation = cancellation;
        TestTreeSortEntity callerRow = TreeCrudTestSupport.node(IDGenerator.RANDOM.generate(),
            null, "caller row", 1L);
        Mono<Integer> caller = service.getRepository().save(callerRow)
            .then(service.getRepository().createUpdate().set("name", "caller changed surviving root")
                .where("id", forest.id("west")).execute())
            .then(service.deleteById(Flux.just(forest.id("east"), forest.id("east-left"))));
        TransactionUtils.tryRunInTransaction(caller, new DefaultTransactionDefinition())
            .takeUntilOther(cancellation.started.asMono()).as(StepVerifier::create)
            .expectComplete().verify(TIMEOUT);

        // 取消不是错误终止；必须等待数据库事务完成回滚，再通过新的查询读取状态。
        cancellation.completion.asMono().as(StepVerifier::create)
            .expectNext(TransactionSynchronization.STATUS_ROLLED_BACK).expectComplete().verify(TIMEOUT);
        assertEquals(2, cancellation.batches.get());
        assertEquals(4, Set.copyOf(cancellation.deletedIds).size());
        assertTrue(cancellation.expectedSubtree.containsAll(cancellation.deletedIds));
        assertEquals(before, TreeCrudTestSupport.snapshot(storedGroup(forest.group)));
        service.findById(callerRow.getId()).as(StepVerifier::create).expectComplete().verify(TIMEOUT);
    }

    private TreeCrudTestSupport.Forest insertForest() {
        TreeCrudTestSupport.Forest forest = new TreeCrudTestSupport.Forest();
        service.insertBatch(Mono.just(forest.childFirst())).as(StepVerifier::create)
            .expectNext(forest.nodes.size()).expectComplete().verify(TIMEOUT);
        TreeCrudTestSupport.assertPersistedTree(storedGroup(forest.group), forest.parents);
        return forest;
    }

    private List<TestTreeSortEntity> storedGroup(String group) {
        return service.query(TreeCrudTestSupport.query(group)).collectList().block(TIMEOUT);
    }

    private Mono<Integer> update(String id, TestTreeSortEntity patch, boolean entityOverload) {
        return entityOverload ? service.updateById(id, patch) : service.updateById(id, Mono.just(patch));
    }

    static class CoverageTreeService extends GenericReactiveCrudService<TestTreeSortEntity, String>
        implements ReactiveTreeSortEntityService<TestTreeSortEntity, String> {
        CoverageTreeService(ReactiveRepository<TestTreeSortEntity, String> repository) {
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
        public int getBufferSize() {
            return 2;
        }

        @Override
        public ReactiveQuery<TestTreeSortEntity> applyTreeScope(TestTreeSortEntity node,
                                                              ReactiveQuery<TestTreeSortEntity> query) {
            return query.and("groupId", node.getGroupId());
        }
    }

    /** 控制器只接入真实 DeletedEvent，第二批 SQL 已删除行后才发取消信号。 */
    static class DeleteEvents {
        private final ReactiveRepository<TestTreeSortEntity, String> repository;
        private volatile DeleteCancellation cancellation;

        DeleteEvents(ReactiveRepository<TestTreeSortEntity, String> repository) {
            this.repository = repository;
        }

        @EventListener
        public void deleted(EntityDeletedEvent<TestTreeSortEntity> event) {
            DeleteCancellation current = cancellation;
            if (event.getEntityType() == TestTreeSortEntity.class && current != null
                && event.getEntity().stream().anyMatch(node -> current.group.equals(node.getGroupId()))) {
                event.async(Mono.defer(() -> {
                    assertTrue(event.getEntity().size() <= 2);
                    current.deletedIds.addAll(TreeCrudTestSupport.ids(event.getEntity()));
                    if (current.batches.incrementAndGet() != 2) {
                        return Mono.empty();
                    }
                    return TransactionSynchronizationManager.forCurrentTransaction().flatMap(manager -> {
                        assertTrue("delete events must share the SQL transaction", manager.isActualTransactionActive());
                        manager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public Mono<Void> afterCompletion(int status) {
                                return Mono.fromRunnable(() -> current.completion.emitValue(status,
                                    Sinks.EmitFailureHandler.FAIL_FAST));
                            }
                        });
                        return repository.createQuery().in("id", List.copyOf(current.deletedIds)).count();
                    }).flatMap(remaining -> {
                        assertEquals("both deleted SQL batches must already be visible", Integer.valueOf(0), remaining);
                        return Mono.fromRunnable(() -> current.started.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST))
                            .then(Mono.<Void>never());
                    });
                }));
            }
        }
    }

    static class DeleteCancellation {
        private final String group;
        private final Set<String> expectedSubtree;
        private final AtomicInteger batches = new AtomicInteger();
        private final List<String> deletedIds = new ArrayList<>();
        private final Sinks.Empty<Void> started = Sinks.empty();
        private final Sinks.One<Integer> completion = Sinks.one();

        DeleteCancellation(String group, Set<String> expectedSubtree) {
            this.group = group;
            this.expectedSubtree = expectedSubtree;
        }
    }

    @TestConfiguration
    static class EventConfiguration {
        @Bean
        CoverageTreeService coverageTreeService(ReactiveRepository<TestTreeSortEntity, String> repository) {
            return new CoverageTreeService(repository);
        }

        @Bean
        DeleteEvents coverageDeleteEvents(ReactiveRepository<TestTreeSortEntity, String> repository) {
            return new DeleteEvents(repository);
        }
    }
}
