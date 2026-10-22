package org.hswebframework.web.crud.service;

import io.r2dbc.spi.ConnectionFactory;
import org.hswebframework.web.api.crud.entity.TreeSupportEntity;
import org.hswebframework.web.crud.TestApplication;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * 验证子树快照读取之后，名称修改仍保留另一独立事务已经提交的移动。
 * 仅暂停测试服务的真实查询下游；树准备、SQL、事件和事务均使用正式实现。
 * 每次测试独立安装和清理 gate，不改变现有首次读取窗口测试。
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApplication.class, ConcurrentPreparedTreePatchRegressionTest.Configuration.class},
    properties = {
        "spring.r2dbc.generate-unique-name=true",
        "spring.r2dbc.name=ConcurrentPreparedTreePatchRegressionTest",
        "spring.r2dbc.pool.enabled=true",
        "spring.r2dbc.pool.initial-size=2",
        "spring.r2dbc.pool.max-size=4"
    })
public class ConcurrentPreparedTreePatchRegressionTest {
    private static final String PATCH = ConcurrentPreparedTreePatchRegressionTest.class.getName();
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    @Qualifier("preparedTree")
    private ControlledTreeService service;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    @Qualifier("connectionFactoryTransactionManager")
    private ReactiveTransactionManager manager;

    @Test
    public void monoNamePatchPreservesMoveCommittedAfterItsSubtreeSnapshot() {
        verifyNamePatchAfterPreparedSubtreeMove(false, false);
    }

    @Test
    public void entityNamePatchPreservesMoveCommittedAfterItsSubtreeSnapshot() {
        verifyNamePatchAfterPreparedSubtreeMove(true, false);
    }

    @Test
    public void monoNamePatchWithEmptyChildrenPreservesMoveCommittedAfterItsSubtreeSnapshot() {
        verifyNamePatchAfterPreparedSubtreeMove(false, true);
    }

    @Test
    public void entityNamePatchWithEmptyChildrenPreservesMoveCommittedAfterItsSubtreeSnapshot() {
        verifyNamePatchAfterPreparedSubtreeMove(true, true);
    }

    private void verifyNamePatchAfterPreparedSubtreeMove(boolean entityOverload, boolean emptyChildren) {
        TestTreeSortEntity parentA = node("parent-a");
        TestTreeSortEntity targetRoot = node("target-root");
        TestTreeSortEntity targetBranch = node("target-branch");
        TestTreeSortEntity target = node("target");
        TestTreeSortEntity branch = node("branch-before");
        TestTreeSortEntity leaf = node("leaf-before");
        branch.setSortIndex(37L);
        leaf.setSortIndex(84L);
        parentA.setChildren(List.of(branch));
        branch.setChildren(List.of(leaf));
        targetRoot.setChildren(List.of(targetBranch));
        targetBranch.setChildren(List.of(target));
        service.save(List.of(parentA, targetRoot))
            .as(StepVerifier::create).expectNextCount(1).verifyComplete();
        List<String> ids = List.of(parentA.getId(), targetRoot.getId(), targetBranch.getId(),
                                   target.getId(), branch.getId(), leaf.getId());
        AtomicReference<Map<String, TestTreeSortEntity>> before = new AtomicReference<>();
        stored(ids).as(StepVerifier::create).assertNext(before::set).verifyComplete();
        SubtreeGate gate = new SubtreeGate(before.get().get(branch.getId()), before.get().get(leaf.getId()),
                                          connectionFactory);
        service.installGate(gate);
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        TransactionalOperator transactions = TransactionalOperator.create(manager, definition);
        TestTreeSortEntity patch = new TestTreeSortEntity();
        patch.setName("branch-after");
        if (emptyChildren) {
            // updateById 的空 children 不删除或替换后代，也不能成为回写旧树字段的理由。
            patch.setChildren(List.of());
        }
        Mono<Integer> update = entityOverload ? service.updateById(branch.getId(), patch)
            : service.updateById(branch.getId(), Mono.just(patch));
        Mono<Void> nameUpdate = transactions.transactional(update)
            .contextWrite(context -> context.put(PATCH, true))
            .doOnNext(total -> assertEquals(Integer.valueOf(1), total)).then();
        AtomicReference<Map<String, TestTreeSortEntity>> moved = new AtomicReference<>();
        Mono<Void> treeMove = gate.read.asMono().flatMap(patchConnection -> transactions.transactional(
            moveSubtreeOnIndependentConnection(patchConnection, branch.getId(), target.getId())
                .then(stored(ids)).doOnNext(tree -> {
                    assertTreeChild(tree.get(target.getId()), tree.get(branch.getId()));
                    assertTreeChild(tree.get(branch.getId()), tree.get(leaf.getId()));
                    assertEquals(Integer.valueOf(4), tree.get(branch.getId()).getLevel());
                    assertEquals(Integer.valueOf(5), tree.get(leaf.getId()).getLevel());
                    moved.set(tree);
                })))
            // 事务的完成信号包含正式提交；只有此后才放行已持有旧子树结果的名称修改。
            .then(Mono.fromRunnable(() -> gate.release.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST)));
        try {
            Mono.when(nameUpdate, treeMove)
                .as(StepVerifier::create).expectComplete().verify(TIMEOUT);
            assertTrue("the gate must pause a real prepared subtree read", gate.paused.get());
            assertNotNull(moved.get());
            stored(ids).as(StepVerifier::create).assertNext(after -> {
                TestTreeSortEntity storedBranch = after.get(branch.getId());
                TestTreeSortEntity storedLeaf = after.get(leaf.getId());
                assertEquals("branch-after", storedBranch.getName());
                assertEquals(target.getId(), storedBranch.getParentId());
                assertEquals(moved.get().get(branch.getId()).getPath(), storedBranch.getPath());
                assertEquals(moved.get().get(branch.getId()).getLevel(), storedBranch.getLevel());
                assertEquals(moved.get().get(leaf.getId()).getPath(), storedLeaf.getPath());
                assertEquals(moved.get().get(leaf.getId()).getLevel(), storedLeaf.getLevel());
                assertTreeChild(after.get(target.getId()), storedBranch);
                assertTreeChild(storedBranch, storedLeaf);
                assertEquals(before.get().get(leaf.getId()).getName(), storedLeaf.getName());
                assertEquals(before.get().get(branch.getId()).getDefaultTest(), storedBranch.getDefaultTest());
                assertEquals(before.get().get(leaf.getId()).getDefaultTest(), storedLeaf.getDefaultTest());
                assertEquals(Long.valueOf(37), storedBranch.getSortIndex());
                assertEquals(Long.valueOf(84), storedLeaf.getSortIndex());
                for (String unchanged : List.of(parentA.getId(), targetRoot.getId(), targetBranch.getId(), target.getId())) {
                    assertTreeState(before.get().get(unchanged), after.get(unchanged));
                }
            }).verifyComplete();
            service.queryIncludeChildren(List.of(branch.getId())).collectList()
                .as(StepVerifier::create).assertNext(nodes ->
                    assertEquals(Set.of(branch.getId(), leaf.getId()), ids(nodes))).verifyComplete();
            service.queryIncludeChildren(List.of(parentA.getId())).collectList()
                .as(StepVerifier::create).assertNext(nodes ->
                    assertEquals(Set.of(parentA.getId()), ids(nodes))).verifyComplete();
            service.queryIncludeChildren(List.of(targetRoot.getId())).collectList()
                .as(StepVerifier::create).assertNext(nodes -> assertEquals(
                    Set.of(targetRoot.getId(), targetBranch.getId(), target.getId(), branch.getId(), leaf.getId()),
                    ids(nodes))).verifyComplete();
        } finally {
            gate.release.tryEmitEmpty();
            service.installGate(null);
            // 精确删除本测试的六个实体，避免清理依赖被测树路径或另一个删除问题。
            service.getRepository().createDelete().in("id", ids).execute()
                .as(StepVerifier::create).expectNext(6).verifyComplete();
        }
    }

    private Mono<?> moveSubtreeOnIndependentConnection(Object patchConnection, String branchId, String targetId) {
        return TransactionSynchronizationManager.forCurrentTransaction().flatMap(transaction -> {
            assertTrue(transaction.isActualTransactionActive());
            Object moveConnection = transaction.getResource(connectionFactory);
            assertNotNull(moveConnection);
            assertNotSame("the writers must use independent transaction connections", patchConnection, moveConnection);
            return service.findById(branchId).flatMap(branch -> {
                branch.setParentId(targetId);
                return service.save(branch);
            });
        });
    }

    private Mono<Map<String, TestTreeSortEntity>> stored(List<String> ids) {
        return service.getRepository().findById(ids).collectMap(TestTreeSortEntity::getId);
    }

    private static TestTreeSortEntity node(String name) {
        TestTreeSortEntity node = new TestTreeSortEntity();
        node.setName(name);
        node.setDefaultTest("business-before");
        return node;
    }

    private static Set<String> ids(Collection<TestTreeSortEntity> nodes) {
        return nodes.stream().map(TestTreeSortEntity::getId).collect(Collectors.toSet());
    }

    private static void assertTreeChild(TestTreeSortEntity parent, TestTreeSortEntity child) {
        assertEquals(parent.getId(), child.getParentId());
        assertEquals(parent.getPath(), TreeSupportEntity.getParentPath(child.getPath()));
        assertEquals(Integer.valueOf(parent.getLevel() + 1), child.getLevel());
    }

    private static void assertTreeState(TestTreeSortEntity before, TestTreeSortEntity after) {
        assertEquals(before.getParentId(), after.getParentId());
        assertEquals(before.getPath(), after.getPath());
        assertEquals(before.getLevel(), after.getLevel());
        assertEquals(before.getName(), after.getName());
        assertEquals(before.getDefaultTest(), after.getDefaultTest());
        assertEquals(before.getSortIndex(), after.getSortIndex());
    }

    /** 每次场景持有一个 gate，只暂停带 PATCH context 的第一次真实子树查询结果。 */
    public static class SubtreeGate {
        private final TestTreeSortEntity branch;
        private final TestTreeSortEntity leaf;
        private final ConnectionFactory factory;
        private final AtomicBoolean paused = new AtomicBoolean();
        private final Sinks.One<Object> read = Sinks.one();
        private final Sinks.Empty<Void> release = Sinks.empty();

        private SubtreeGate(TestTreeSortEntity branch, TestTreeSortEntity leaf, ConnectionFactory factory) {
            this.branch = branch;
            this.leaf = leaf;
            this.factory = factory;
        }

        private Flux<TestTreeSortEntity> afterRead(List<TestTreeSortEntity> nodes) {
            assertEquals(Set.of(branch.getId(), leaf.getId()), ids(nodes));
            Map<String, TestTreeSortEntity> snapshot = nodes.stream()
                .collect(Collectors.toMap(TestTreeSortEntity::getId, node -> node));
            assertTreeState(branch, snapshot.get(branch.getId()));
            assertTreeState(leaf, snapshot.get(leaf.getId()));
            return TransactionSynchronizationManager.forCurrentTransaction().flatMapMany(transaction -> {
                assertTrue(transaction.isActualTransactionActive());
                Object connection = transaction.getResource(factory);
                assertNotNull(connection);
                read.emitValue(connection, Sinks.EmitFailureHandler.FAIL_FAST);
                return release.asMono().thenMany(Flux.fromIterable(nodes));
            });
        }
    }

    /** 使用正式服务准备树，只有测试事务的完整查询结果被延迟交给 helper。 */
    public static class ControlledTreeService extends TestTreeSortEntityService {
        private volatile SubtreeGate gate;

        public void installGate(SubtreeGate gate) {
            this.gate = gate;
        }

        @Override
        public Flux<TestTreeSortEntity> queryIncludeChildren(Collection<String> ids) {
            return Flux.deferContextual(context -> {
                SubtreeGate current = gate;
                Flux<TestTreeSortEntity> queried = super.queryIncludeChildren(ids);
                if (current != null && context.hasKey(PATCH) && ids.contains(current.branch.getId())
                    && current.paused.compareAndSet(false, true)) {
                    // 本场景只有 branch 和 leaf；收集真实结果之后暂停，不拦首次存在性读取。
                    return queried.collectList().flatMapMany(current::afterRead);
                }
                return queried;
            });
        }
    }

    @TestConfiguration
    public static class Configuration {
        @Bean
        public ControlledTreeService preparedTree() {
            return new ControlledTreeService();
        }
    }
}
