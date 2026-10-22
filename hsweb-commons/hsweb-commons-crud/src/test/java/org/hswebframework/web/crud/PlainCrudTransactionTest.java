package org.hswebframework.web.crud;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.hswebframework.web.crud.entity.CustomTestEntity;
import org.hswebframework.web.crud.entity.TestEntity;
import org.hswebframework.web.crud.events.EntityDeletedEvent;
import org.hswebframework.web.crud.events.EntityModifyEvent;
import org.hswebframework.web.crud.service.CustomTestCustom;
import org.hswebframework.web.crud.service.TestEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionSynchronization;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 普通 DSL 沿用调用方事务；完整业务链经 Spring 代理明确声明事务。
 * 事件控制器只装配在本测试上下文，不修改共享应用、实体或生产事务配置。
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    TestApplication.class, TestEntityService.class, CustomTestCustom.class,
    PlainCrudTransactionTest.EventConfiguration.class
}, properties = {
    "spring.r2dbc.generate-unique-name=true",
    "spring.r2dbc.name=PlainCrudTransactionTest"
})
public class PlainCrudTransactionTest {

    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    private TestEntityService service;

    @Autowired
    private ControlledEntityEvents events;

    @Autowired
    private BusinessWrites business;

    @Autowired
    private DatabaseOperator database;

    private CountingReactiveSqlExecutor sql;

    @Before
    public void countRealSql() {
        sql = new CountingReactiveSqlExecutor(database.getMetadata().getTable("s_test").orElseThrow());
    }

    @After
    public void resetEvents() {
        events.failModifyId = null;
        events.modifyCompletion = null;
        events.deleteCancellation = null;
        events.callerId = null;
        events.noTransactionId = null;
        events.observedModify.set(0);
        events.observedDelete.set(0);
        sql.close();
    }

    @Test
    public void updateRollsBackWhenModifyEventFails() {
        TestEntity entity = insert("before-update");
        TestEntity caller = callerRow();
        events.failModifyId = entity.getId();
        events.callerId = caller.getId();
        events.modifyCompletion = Sinks.one();
        assertTrue("the business boundary must be a Spring transaction proxy", AopUtils.isAopProxy(business));
        sql.reset();

        business.update(entity.getId(), caller)
               .as(StepVerifier::create)
               .expectErrorMatches(error -> error == events.modifyFailure)
               .verify(VERIFY_TIMEOUT);

        events.modifyCompletion.asMono().as(StepVerifier::create)
            .expectNext(TransactionSynchronization.STATUS_ROLLED_BACK).expectComplete().verify(VERIFY_TIMEOUT);
        assertEquals(1, sql.count("update"));
        service.getRepository().findById(caller.getId()).as(StepVerifier::create).verifyComplete();

        service.findById(entity.getId())
               .map(TestEntity::getName)
               .as(StepVerifier::create)
               .expectNext("before-update")
               .expectComplete()
               .verify(VERIFY_TIMEOUT);
    }

    @Test
    public void deleteRollsBackWhenCancelledAfterSqlAndEventStart() {
        TestEntity entity = insert("before-delete");
        DeleteCancellation cancellation = new DeleteCancellation(entity.getId());
        TestEntity caller = callerRow();
        events.deleteCancellation = cancellation;
        events.callerId = caller.getId();
        assertTrue("the business boundary must be a Spring transaction proxy", AopUtils.isAopProxy(business));
        sql.reset();

        // 删除事件在当前事务中确认行已消失后发信号，再由下游取消挂起的执行流。
        business.delete(entity.getId(), caller)
               .takeUntilOther(cancellation.started.asMono())
               .as(StepVerifier::create)
               .expectComplete()
               .verify(VERIFY_TIMEOUT);

        // 取消的清理是异步的；等待真实回滚完成信号后再验证数据库，不使用延时或轮询。
        cancellation.completion.asMono()
                               .as(StepVerifier::create)
                               .expectNext(TransactionSynchronization.STATUS_ROLLED_BACK)
                               .expectComplete()
                               .verify(VERIFY_TIMEOUT);

        assertEquals(1, sql.count("delete"));
        service.getRepository().findById(caller.getId()).as(StepVerifier::create).verifyComplete();

        service.findById(entity.getId())
               .map(TestEntity::getName)
               .as(StepVerifier::create)
               .expectNext("before-delete")
               .expectComplete()
               .verify(VERIFY_TIMEOUT);
    }

    @Test
    public void ordinaryDslWithoutBusinessTransactionPreservesHooksAndExecutesOnce() {
        TestEntity entity = insert("ordinary-before");
        events.noTransactionId = entity.getId();
        List<String> hooks = new ArrayList<>();
        sql.reset();
        service.createUpdate().set("name", "ordinary-after").where("id", entity.getId())
            .onExecute((update, operation) -> verifyNoTransaction().then(Mono.defer(() -> {
                hooks.add("update-before");
                return operation;
            })).doOnNext(count -> hooks.add("update-after:" + count)))
            .execute().as(StepVerifier::create).expectNext(1).verifyComplete();
        assertEquals(List.of("update-before", "update-after:1"), hooks);
        assertEquals(1, sql.count("update"));
        assertEquals(1, events.observedModify.get());
        service.getRepository().findById(entity.getId()).map(TestEntity::getName)
            .as(StepVerifier::create).expectNext("ordinary-after").verifyComplete();

        hooks.clear();
        sql.reset();
        service.createDelete().where("id", entity.getId())
            .onExecute((delete, operation) -> verifyNoTransaction().then(Mono.defer(() -> {
                hooks.add("delete-before");
                return operation;
            })).doOnNext(count -> hooks.add("delete-after:" + count)))
            .execute().as(StepVerifier::create).expectNext(1).verifyComplete();
        assertEquals(List.of("delete-before", "delete-after:1"), hooks);
        assertEquals(1, sql.count("delete"));
        assertEquals(1, events.observedDelete.get());
        service.getRepository().findById(entity.getId()).as(StepVerifier::create).verifyComplete();

        sql.reset();
        AtomicInteger zeroHooks = new AtomicInteger();
        service.createUpdate().set("name", "absent").where("id", entity.getId())
            .onExecute((update, operation) -> verifyNoTransaction().then(operation)
                .doOnNext(count -> { assertEquals(Integer.valueOf(0), count); zeroHooks.incrementAndGet(); }))
            .execute().then(service.createDelete().where("id", entity.getId())
                .onExecute((delete, operation) -> verifyNoTransaction().then(operation)
                    .doOnNext(count -> { assertEquals(Integer.valueOf(0), count); zeroHooks.incrementAndGet(); }))
                .execute()).as(StepVerifier::create).expectNext(0).verifyComplete();
        assertEquals(2, zeroHooks.get());
        assertEquals(1, sql.count("update"));
        assertEquals(1, sql.count("delete"));
        assertEquals(1, events.observedModify.get());
        assertEquals(1, events.observedDelete.get());
    }

    @Test
    public void ordinaryDslAfterEventFailureDoesNotPromiseImplicitRollback() {
        TestEntity entity = insert("autocommit-before");
        events.noTransactionId = entity.getId();
        events.failModifyId = entity.getId();
        sql.reset();
        service.createUpdate().set("name", "autocommit-after").where("id", entity.getId())
            .execute().as(StepVerifier::create).expectErrorMatches(error -> error == events.modifyFailure)
            .verify(VERIFY_TIMEOUT);
        assertEquals(1, events.observedModify.get());
        assertEquals(1, sql.count("update"));
        service.getRepository().findById(entity.getId()).map(TestEntity::getName)
            .as(StepVerifier::create).expectNext("autocommit-after").verifyComplete();
    }

    private static Mono<Void> verifyNoTransaction() {
        return TransactionSynchronizationManager.forCurrentTransaction()
            .map(manager -> !manager.isActualTransactionActive())
            .onErrorReturn(NoTransactionException.class, true)
            .doOnNext(absent -> assertTrue("ordinary DSL must not open a Spring transaction", absent)).then();
    }

    private TestEntity callerRow() {
        CustomTestEntity caller = new CustomTestEntity();
        caller.setId(IDGenerator.RANDOM.generate());
        caller.setName("caller row");
        caller.setAge(2);
        return caller;
    }

    private TestEntity insert(String name) {
        CustomTestEntity entity = new CustomTestEntity();
        entity.setId(IDGenerator.RANDOM.generate());
        entity.setName(name);
        entity.setAge(1);
        service.insert(entity)
               .as(StepVerifier::create)
               .expectNext(1)
               .expectComplete()
               .verify(VERIFY_TIMEOUT);
        return entity;
    }

    @TestConfiguration
    static class EventConfiguration {
        @Bean
        ControlledEntityEvents controlledEntityEvents(TestEntityService service) {
            return new ControlledEntityEvents(service);
        }

        @Bean
        BusinessWrites businessWrites(TestEntityService service) {
            return new BusinessWrites(service);
        }
    }

    /** 返回完整的业务 Publisher；事务由此代理边界管理，不放在 DSL 工厂。 */
    public static class BusinessWrites {
        private final TestEntityService service;

        BusinessWrites(TestEntityService service) {
            this.service = service;
        }

        @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
        public Mono<Integer> update(String id, TestEntity caller) {
            return service.getRepository().insert(Mono.just(caller))
                .then(service.createUpdate().set("name", "after-update").where("id", id).execute());
        }

        @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
        public Mono<Integer> delete(String id, TestEntity caller) {
            return service.getRepository().insert(Mono.just(caller))
                .then(service.createDelete().where("id", id).execute());
        }
    }

    /** 每个用例只控制自己插入的行；其他测试及实体事件使用原有行为。 */
    static class ControlledEntityEvents {
        private final TestEntityService service;
        private final IllegalStateException modifyFailure = new IllegalStateException("modify event failed");
        private volatile String failModifyId;
        private volatile Sinks.One<Integer> modifyCompletion;
        private volatile String callerId;
        private volatile String noTransactionId;
        private final AtomicInteger observedModify = new AtomicInteger();
        private final AtomicInteger observedDelete = new AtomicInteger();
        private volatile DeleteCancellation deleteCancellation;

        ControlledEntityEvents(TestEntityService service) {
            this.service = service;
        }

        @EventListener
        public void modified(EntityModifyEvent<TestEntity> event) {
            if (event.getAfter().stream().anyMatch(entity -> entity.getId().equals(noTransactionId))) {
                event.async(verifyNoTransaction().doOnSuccess(ignored -> observedModify.incrementAndGet()));
            }
            if (event.getAfter().stream().anyMatch(entity -> entity.getId().equals(failModifyId))) {
                Sinks.One<Integer> completion = modifyCompletion;
                event.async((completion == null ? Mono.<Void>empty() :
                    TransactionSynchronizationManager.forCurrentTransaction().flatMap(manager -> {
                        assertTrue(manager.isActualTransactionActive());
                        manager.registerSynchronization(completion(completion));
                        return service.getRepository().findById(callerId)
                            .switchIfEmpty(Mono.error(new AssertionError("caller write must share the transaction")))
                            .then(service.getRepository().findById(failModifyId))
                            .switchIfEmpty(Mono.error(new AssertionError("modify event must observe the updated row")))
                            .doOnNext(entity -> assertEquals("after-update", entity.getName())).then();
                    })).then(Mono.error(modifyFailure)));
            }
        }

        @EventListener
        public void deleted(EntityDeletedEvent<TestEntity> event) {
            if (event.getEntity().stream().anyMatch(entity -> entity.getId().equals(noTransactionId))) {
                event.async(verifyNoTransaction().doOnSuccess(ignored -> observedDelete.incrementAndGet()));
            }
            DeleteCancellation cancellation = deleteCancellation;
            if (cancellation != null && event.getEntity().stream()
                                              .anyMatch(entity -> entity.getId().equals(cancellation.id))) {
                event.async(awaitCancellation(cancellation));
            }
        }

        private Mono<Void> awaitCancellation(DeleteCancellation cancellation) {
            return TransactionSynchronizationManager.forCurrentTransaction()
                .flatMap(manager -> {
                    if (!manager.isActualTransactionActive()) {
                        return Mono.error(new AssertionError("delete event requires the SQL transaction"));
                    }
                    manager.registerSynchronization(completion(cancellation.completion));
                    return service.getRepository().findById(callerId)
                        .switchIfEmpty(Mono.error(new AssertionError("caller write must share the transaction")))
                        .then(service.getRepository().findById(cancellation.id).hasElement());
                })
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new AssertionError("delete event started before the SQL deleted the row"));
                    }
                    return Mono.fromRunnable(() -> cancellation.started.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST))
                               .then(Mono.never());
                });
        }

        private TransactionSynchronization completion(Sinks.One<Integer> signal) {
            return new TransactionSynchronization() {
                @Override
                public Mono<Void> afterCompletion(int status) {
                    return Mono.fromRunnable(() -> signal.emitValue(status, Sinks.EmitFailureHandler.FAIL_FAST));
                }
            };
        }
    }

    /** 信号分离事件启动和事务回滚完成，避免在 SQL 执行前取消或提前读取数据库。 */
    static class DeleteCancellation {
        private final String id;
        private final Sinks.Empty<Void> started = Sinks.empty();
        private final Sinks.One<Integer> completion = Sinks.one();

        DeleteCancellation(String id) {
            this.id = id;
        }
    }
}
