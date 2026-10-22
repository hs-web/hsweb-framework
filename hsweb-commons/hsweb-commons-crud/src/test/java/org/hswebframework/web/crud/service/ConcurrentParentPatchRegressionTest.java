package org.hswebframework.web.crud.service;

import io.r2dbc.spi.ConnectionFactory;
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
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/** 稀疏业务修改不能用旧快照覆盖另一真实事务已提交的树移动。 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApplication.class, ConcurrentParentPatchRegressionTest.Configuration.class},
    properties = {
        "spring.r2dbc.generate-unique-name=true",
        "spring.r2dbc.name=R9ConcurrentParentPatchRegressionTest",
        "spring.r2dbc.pool.enabled=true",
        "spring.r2dbc.pool.initial-size=2",
        "spring.r2dbc.pool.max-size=4"
    })
public class ConcurrentParentPatchRegressionTest {
    private static final String PATCH = ConcurrentParentPatchRegressionTest.class.getName();
    @Autowired @Qualifier("controlledTree") private ControlledTreeService service;
    @Autowired private ConnectionFactory connectionFactory;
    @Autowired @Qualifier("connectionFactoryTransactionManager") private ReactiveTransactionManager manager;

    @Test
    public void monoNamePatchPreservesTheOtherTransactionsCommittedParentAndPaths() {
        verifyNamePatchAfterParentMove(false);
    }

    @Test
    public void entityNamePatchPreservesTheOtherTransactionsCommittedParentAndPaths() {
        verifyNamePatchAfterParentMove(true);
    }

    @Test
    public void monoUpdateCannotReinsertTargetDeletedAfterItsInitialRead() {
        verifyUpdateAfterTargetDeletion(false);
    }

    @Test
    public void entityUpdateCannotReinsertTargetDeletedAfterItsInitialRead() {
        verifyUpdateAfterTargetDeletion(true);
    }

    private void verifyUpdateAfterTargetDeletion(boolean entityOverload) {
        TestTreeSortEntity target = node("target-before-delete");
        service.save(target).as(StepVerifier::create).expectNextCount(1).verifyComplete();
        Gate gate = new Gate(target.getId(), null, connectionFactory);
        service.installGate(gate);
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        TransactionalOperator transactions = TransactionalOperator.create(manager, definition);

        TestTreeSortEntity patch = node("target-after-delete-" + target.getId());
        TestTreeSortEntity child = node("deleted-target-child-" + target.getId());
        patch.setChildren(List.of(child));
        AtomicReference<Integer> updated = new AtomicReference<>();
        Mono<Void> update = transactions.transactional(entityOverload ? service.updateById(target.getId(), patch)
            : service.updateById(target.getId(), Mono.just(patch)))
            .contextWrite(context -> context.put(PATCH, true)).doOnNext(updated::set).then();
        Mono<Void> deletion = gate.read.asMono().flatMap(updateConnection -> transactions.transactional(
            TransactionSynchronizationManager.forCurrentTransaction().flatMap(tx -> {
                assertTrue(tx.isActualTransactionActive());
                Object deleteConnection = tx.getResource(connectionFactory);
                assertNotNull(deleteConnection);
                assertNotSame("deletion and update require real independent connections", updateConnection, deleteConnection);
                return service.deleteById(target.getId()).doOnNext(deleted -> assertEquals(Integer.valueOf(1), deleted));
            })))
            // 放行发生在删除事务正式完成之后，第二次图读取必须看到目标已不存在。
            .then(Mono.fromRunnable(() -> gate.release.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST)));
        try {
            Mono.when(update, deletion).as(StepVerifier::create).expectComplete().verify(Duration.ofSeconds(10));
            service.getRepository().createQuery().in("name", List.of(patch.getName(), child.getName())).count()
                .as(StepVerifier::create).assertNext(count -> {
                    assertEquals("update-only target must stay absent; update count=" + updated.get(), Integer.valueOf(0), count);
                    assertEquals(Integer.valueOf(0), updated.get());
                }).verifyComplete();
            service.getRepository().findById(target.getId()).as(StepVerifier::create).verifyComplete();
        } finally {
            gate.release.tryEmitEmpty();
            service.installGate(null);
        }
    }

    private void verifyNamePatchAfterParentMove(boolean entityOverload) {
        TestTreeSortEntity parentA = node("parent-a");
        TestTreeSortEntity parentB = node("parent-b");
        TestTreeSortEntity branch = node("branch-before");
        TestTreeSortEntity leaf = node("leaf-before");
        parentA.setChildren(List.of(branch));
        branch.setChildren(List.of(leaf));
        service.save(List.of(parentA, parentB)).as(StepVerifier::create).expectNextCount(1).verifyComplete();
        Gate gate = new Gate(branch.getId(), parentA.getId(), connectionFactory);
        service.installGate(gate);
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        TransactionalOperator transactions = TransactionalOperator.create(manager, definition);

        TestTreeSortEntity patch = new TestTreeSortEntity();
        patch.setName("branch-after");
        Mono<Void> nameUpdate = transactions.transactional(entityOverload ? service.updateById(branch.getId(), patch)
            : service.updateById(branch.getId(), Mono.just(patch)))
            .contextWrite(context -> context.put(PATCH, true)).then();
        AtomicReference<Map<String, TestTreeSortEntity>> moved = new AtomicReference<>();
        Mono<Void> treeMove = gate.read.asMono().flatMap(patchConnection -> transactions.transactional(
            TransactionSynchronizationManager.forCurrentTransaction().flatMap(tx -> {
                assertTrue(tx.isActualTransactionActive());
                Object moveConnection = tx.getResource(connectionFactory);
                assertNotNull(moveConnection);
                assertNotSame("the two writers require real independent connections", patchConnection, moveConnection);
                return service.findById(branch.getId()).flatMap(stored -> {
                    stored.setParentId(parentB.getId());
                    return service.save(stored);
                });
            }).then(service.getRepository().findById(List.of(branch.getId(), leaf.getId()))
                .collectMap(TestTreeSortEntity::getId)).doOnNext(moved::set)))
            // This callback follows the move's transaction completion, before the name writer is released.
            .then(Mono.fromRunnable(() -> gate.release.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST)));
        try {
            Mono.when(nameUpdate, treeMove).as(StepVerifier::create).expectComplete().verify(Duration.ofSeconds(10));
            service.getRepository().findById(List.of(branch.getId(), leaf.getId())).collectMap(TestTreeSortEntity::getId)
                .as(StepVerifier::create).assertNext(after -> {
                    TestTreeSortEntity storedBranch = after.get(branch.getId());
                    TestTreeSortEntity storedLeaf = after.get(leaf.getId());
                    assertEquals("branch-after", storedBranch.getName());
                    assertEquals(parentB.getId(), storedBranch.getParentId());
                    assertTrue("branch path must match its persisted parent B",
                        storedBranch.getPath().startsWith(parentB.getPath() + "-"));
                    assertEquals(moved.get().get(branch.getId()).getPath(), storedBranch.getPath());
                    assertEquals(moved.get().get(leaf.getId()).getPath(), storedLeaf.getPath());
                    assertEquals(branch.getId(), storedLeaf.getParentId());
                    assertTrue(storedLeaf.getPath().startsWith(storedBranch.getPath() + "-"));
                }).expectComplete().verify(Duration.ofSeconds(10));
        } finally {
            service.installGate(null);
        }
    }

    private static TestTreeSortEntity node(String name) {
        TestTreeSortEntity node = new TestTreeSortEntity();
        node.setName(name);
        node.setDefaultTest("business-before");
        return node;
    }

    public static class Gate {
        private final String id;
        private final String parent;
        private final ConnectionFactory factory;
        private final Sinks.One<Object> read = Sinks.one();
        private final Sinks.Empty<Void> release = Sinks.empty();
        public Gate(String id, String parent, ConnectionFactory factory) {
            this.id = id;
            this.parent = parent;
            this.factory = factory;
        }
        Mono<TestTreeSortEntity> afterRead(TestTreeSortEntity entity) {
            return TransactionSynchronizationManager.forCurrentTransaction().flatMap(tx -> {
                assertTrue(tx.isActualTransactionActive());
                assertEquals(parent, entity.getParentId());
                Object connection = tx.getResource(factory);
                assertNotNull(connection);
                read.emitValue(connection, Sinks.EmitFailureHandler.FAIL_FAST);
                return release.asMono().thenReturn(entity);
            });
        }
    }

    public static class ControlledTreeService extends TestTreeSortEntityService {
        private volatile Gate gate;
        public void installGate(Gate gate) { this.gate = gate; }
        @Override
        public Mono<TestTreeSortEntity> findById(String id) {
            return super.findById(id).flatMap(entity -> Mono.deferContextual(context -> {
                Gate current = gate;
                return current != null && current.id.equals(id) && context.hasKey(PATCH)
                    ? current.afterRead(entity) : Mono.just(entity);
            }));
        }
    }

    @TestConfiguration
    public static class Configuration {
        @Bean public ControlledTreeService controlledTree() { return new ControlledTreeService(); }
    }
}
