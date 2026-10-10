package org.hswebframework.web.crud;

import org.hswebframework.web.crud.entity.CustomTestEntity;
import org.hswebframework.web.crud.entity.TestEntity;
import org.hswebframework.web.crud.events.EntityDeletedEvent;
import org.hswebframework.web.crud.events.EntityModifyEvent;
import org.hswebframework.web.crud.service.CustomTestCustom;
import org.hswebframework.web.crud.service.TestEntityService;
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
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * 使用现有普通 CRUD 服务验证 DSL 的 SQL 与异步实体事件属于同一事务。
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

    @After
    public void resetEvents() {
        events.failModifyId = null;
        events.deleteCancellation = null;
    }

    @Test
    public void updateRollsBackWhenModifyEventFails() {
        TestEntity entity = insert("before-update");
        events.failModifyId = entity.getId();

        service.createUpdate()
               .set(TestEntity::getName, "after-update")
               .where(TestEntity::getId, entity.getId())
               .execute()
               .as(StepVerifier::create)
               .expectErrorMatches(error -> error == events.modifyFailure)
               .verify(VERIFY_TIMEOUT);

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
        events.deleteCancellation = cancellation;

        // 删除事件在当前事务中确认行已消失后发信号，再由下游取消挂起的执行流。
        service.createDelete()
               .where(TestEntity::getId, entity.getId())
               .execute()
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

        service.findById(entity.getId())
               .map(TestEntity::getName)
               .as(StepVerifier::create)
               .expectNext("before-delete")
               .expectComplete()
               .verify(VERIFY_TIMEOUT);
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
    }

    /** 每个用例只控制自己插入的行；其他测试及实体事件使用原有行为。 */
    static class ControlledEntityEvents {
        private final TestEntityService service;
        private final IllegalStateException modifyFailure = new IllegalStateException("modify event failed");
        private volatile String failModifyId;
        private volatile DeleteCancellation deleteCancellation;

        ControlledEntityEvents(TestEntityService service) {
            this.service = service;
        }

        @EventListener
        public void modified(EntityModifyEvent<TestEntity> event) {
            if (event.getAfter().stream().anyMatch(entity -> entity.getId().equals(failModifyId))) {
                event.async(Mono.error(modifyFailure));
            }
        }

        @EventListener
        public void deleted(EntityDeletedEvent<TestEntity> event) {
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
                    manager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public Mono<Void> afterCompletion(int status) {
                            return Mono.fromRunnable(() -> cancellation.completion.emitValue(
                                status, Sinks.EmitFailureHandler.FAIL_FAST));
                        }
                    });
                    return service.getRepository().findById(cancellation.id).hasElement();
                })
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new AssertionError("delete event started before the SQL deleted the row"));
                    }
                    return Mono.fromRunnable(() -> cancellation.started.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST))
                               .then(Mono.never());
                });
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
