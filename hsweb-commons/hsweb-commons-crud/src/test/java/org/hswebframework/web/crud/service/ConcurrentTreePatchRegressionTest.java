package org.hswebframework.web.crud.service;

import io.r2dbc.spi.ConnectionFactory;
import org.hswebframework.web.crud.TestApplication;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.id.IDGenerator;
import org.junit.After;
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

import static org.junit.Assert.*;

/** 两个真实事务读取同一旧行后，独立业务字段的稀疏修改不能互相覆盖。 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApplication.class, ConcurrentTreePatchRegressionTest.Configuration.class},
    properties = {
        "spring.r2dbc.generate-unique-name=true",
        "spring.r2dbc.name=ConcurrentTreePatchRegressionTest",
        "spring.r2dbc.pool.enabled=true",
        "spring.r2dbc.pool.initial-size=2",
        "spring.r2dbc.pool.max-size=4"
    })
public class ConcurrentTreePatchRegressionTest {

    private static final String WRITER = ConcurrentTreePatchRegressionTest.class.getName();
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    private ControlledTreeService service;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    @Qualifier("connectionFactoryTransactionManager")
    private ReactiveTransactionManager transactionManager;

    @After
    public void resetGate() {
        service.setReadGate(null);
    }

    @Test
    public void monoPatchesInSeparateTransactionsKeepBothBusinessFields() {
        verifyIndependentPatches(false);
    }

    @Test
    public void entityPatchesInSeparateTransactionsKeepBothBusinessFields() {
        verifyIndependentPatches(true);
    }

    private void verifyIndependentPatches(boolean entityOverload) {
        TestTreeSortEntity root = new TestTreeSortEntity();
        root.setId(IDGenerator.RANDOM.generate());
        root.setName("name-before");
        root.setDefaultTest("business-before");
        root.setSortIndex(23L);
        service.save(root).as(StepVerifier::create).expectNextCount(1).verifyComplete();
        ReadGate gate = new ReadGate(root.getId(), connectionFactory);
        service.setReadGate(gate);

        TestTreeSortEntity namePatch = new TestTreeSortEntity();
        namePatch.setName("name-after");
        TestTreeSortEntity businessPatch = new TestTreeSortEntity();
        businessPatch.setDefaultTest("business-after");
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        TransactionalOperator transactions = TransactionalOperator.create(transactionManager, definition);

        Mono<Integer> firstUpdate = entityOverload ? service.updateById(root.getId(), namePatch)
            : service.updateById(root.getId(), Mono.just(namePatch));
        Mono<Integer> secondUpdate = entityOverload ? service.updateById(root.getId(), businessPatch)
            : service.updateById(root.getId(), Mono.just(businessPatch));
        // 两次操作均在自己的连接和事务中读取旧值。第一事务完整提交后才放行第二次保存。
        Mono<Void> first = transactions.transactional(firstUpdate)
            .contextWrite(context -> context.put(WRITER, "first"))
            .doOnNext(total -> assertEquals(Integer.valueOf(1), total))
            .then(Mono.fromRunnable(() -> gate.secondRelease.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST)));
        Mono<Void> second = transactions.transactional(secondUpdate)
            .contextWrite(context -> context.put(WRITER, "second"))
            .doOnNext(total -> assertEquals(Integer.valueOf(1), total)).then();
        Mono<Void> releaseFirst = Mono.zip(gate.firstRead.asMono(), gate.secondRead.asMono())
            .doOnNext(pair -> {
                assertEquals("name-before", pair.getT1().name);
                assertEquals("business-before", pair.getT1().business);
                assertEquals("name-before", pair.getT2().name);
                assertEquals("business-before", pair.getT2().business);
                assertNotSame("the writers must hold different transaction connections",
                    pair.getT1().transactionResource, pair.getT2().transactionResource);
            })
            .then(Mono.fromRunnable(() -> gate.firstRelease.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST)));

        Mono.when(first, second, releaseFirst).as(StepVerifier::create).expectComplete().verify(VERIFY_TIMEOUT);
        service.findById(root.getId()).as(StepVerifier::create).assertNext(after -> {
            assertEquals("name-after", after.getName());
            assertEquals("business-after", after.getDefaultTest());
            assertNull(after.getParentId());
            assertEquals(root.getPath(), after.getPath());
            assertEquals(root.getLevel(), after.getLevel());
            assertEquals(root.getSortIndex(), after.getSortIndex());
        }).expectComplete().verify(VERIFY_TIMEOUT);
    }

    static class ReadSnapshot {
        final String name;
        final String business;
        final Object transactionResource;

        ReadSnapshot(TestTreeSortEntity entity, Object transactionResource) {
            this.name = entity.getName();
            this.business = entity.getDefaultTest();
            this.transactionResource = transactionResource;
        }
    }

    /** 只暂停真实仓储首次读取的下游，不替换数据库结果或树保存实现。 */
    static class ReadGate {
        final String id;
        final ConnectionFactory connectionFactory;
        final Sinks.One<ReadSnapshot> firstRead = Sinks.one();
        final Sinks.One<ReadSnapshot> secondRead = Sinks.one();
        final Sinks.Empty<Void> firstRelease = Sinks.empty();
        final Sinks.Empty<Void> secondRelease = Sinks.empty();

        ReadGate(String id, ConnectionFactory connectionFactory) {
            this.id = id;
            this.connectionFactory = connectionFactory;
        }

        Mono<TestTreeSortEntity> afterRead(String writer, TestTreeSortEntity entity) {
            return TransactionSynchronizationManager.forCurrentTransaction().flatMap(manager -> {
                assertTrue("the old row must be read inside the writer transaction", manager.isActualTransactionActive());
                Object resource = manager.getResource(connectionFactory);
                assertNotNull("the writer transaction must bind its real connection", resource);
                ReadSnapshot snapshot = new ReadSnapshot(entity, resource);
                if ("first".equals(writer)) {
                    firstRead.emitValue(snapshot, Sinks.EmitFailureHandler.FAIL_FAST);
                    return firstRelease.asMono().thenReturn(entity);
                }
                secondRead.emitValue(snapshot, Sinks.EmitFailureHandler.FAIL_FAST);
                return secondRelease.asMono().thenReturn(entity);
            });
        }
    }

    static class ControlledTreeService extends GenericReactiveTreeSupportCrudService<TestTreeSortEntity, String> {
        private volatile ReadGate gate;

        // 通过代理调用方法设置真实服务目标，不能直接写事务代理对象上的字段。
        public void setReadGate(ReadGate gate) {
            this.gate = gate;
        }

        @Override
        public Mono<TestTreeSortEntity> findById(String id) {
            return super.findById(id).flatMap(entity -> Mono.deferContextual(context -> {
                ReadGate current = gate;
                return current != null && current.id.equals(id) && context.hasKey(WRITER)
                    ? current.afterRead(context.get(WRITER), entity) : Mono.just(entity);
            }));
        }

        @Override
        public IDGenerator<String> getIDGenerator() {
            return IDGenerator.MD5;
        }

        @Override
        public void setChildren(TestTreeSortEntity entity, List<TestTreeSortEntity> children) {
            entity.setChildren(children);
        }
    }

    @TestConfiguration
    static class Configuration {
        @Bean
        ControlledTreeService controlledTreeService() {
            return new ControlledTreeService();
        }
    }
}
