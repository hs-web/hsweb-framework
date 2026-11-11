package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.mapping.ReactiveQueryResultContext;
import org.hswebframework.ezorm.rdb.mapping.ReactiveQueryResultTransformer;
import org.hswebframework.web.crud.CountingReactiveSqlExecutor;
import org.hswebframework.web.crud.TestApplication;
import org.hswebframework.web.crud.entity.TestEntity;
import org.hswebframework.web.crud.utils.TransactionUtils;
import org.hswebframework.web.id.IDGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;

@SpringBootTest(classes = TestApplication.class, args = "--hsweb.cache.type=guava")
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class GenericReactiveCacheSupportCrudServiceTest {

    @Autowired
    private TestCacheEntityService entityService;

    @Autowired
    private DatabaseOperator database;

    private CountingReactiveSqlExecutor sql;

    @Before
    public void countRealSql() {
        sql = new CountingReactiveSqlExecutor(database.getMetadata());
    }

    @After
    public void restoreSqlExecutor() {
        sql.close();
    }

    @Test
    public void contextDependentResultsBypassIdentityAndAllEntityCaches() {
        TestEntity entity = TestEntity.of("cache-context", 10, "cache marker");
        entityService.insert(entity).block();
        entityService.findById(entity.getId()).block();
        entityService.getCacheAll().collectList().block();
        var table = database.getMetadata().getTable("s_test").orElseThrow();
        table.addFeature(new ReactiveQueryResultTransformer() {
            @Override
            public <T> Flux<T> transform(Flux<T> rows, ReactiveQueryResultContext context) {
                return Flux.deferContextual(subscriber -> rows.doOnNext(row ->
                    ((TestEntity) row).setName(subscriber.getOrDefault("view", "default"))));
            }
        });
        try {
            assertFalse(entityService.getRepository().isResultCacheable());
            for (String view : List.of("scope-A", "scope-B", "permission-denied")) {
                assertEquals(view, entityService.findById(entity.getId()).contextWrite(c -> c.put("view", view)).block().getName());
                assertEquals(view, entityService.getCacheAll().filter(row -> entity.getId().equals(row.getId()))
                    .single().contextWrite(c -> c.put("view", view)).block().getName());
            }
        } finally {
            table.getFeatures().remove(ReactiveQueryResultTransformer.ID_VALUE);
            entityService.getCache().clear().block();
        }
        assertTrue(entityService.getRepository().isResultCacheable());
        entityService.findById(entity.getId()).block();
        sql.reset();
        entityService.findById(entity.getId()).block();
        assertEquals(0, sql.count("select"));
    }

    @Test
    public void cacheSafeTransformationRetainsCacheHitsAndWriteInvalidation() {
        TestEntity entity = TestEntity.of("source", 10, "cache-pure");
        entityService.insert(entity).block();
        var table = database.getMetadata().getTable("s_test").orElseThrow();
        table.addFeature(new ReactiveQueryResultTransformer() {
            @Override
            public boolean isResultCacheable() {
                return true;
            }

            @Override
            public <T> Flux<T> transform(Flux<T> rows, ReactiveQueryResultContext context) {
                return rows.doOnNext(row -> {
                    TestEntity value = (TestEntity) row;
                    value.setName("cached:" + value.getName());
                });
            }
        });
        try {
            assertTrue(entityService.getRepository().isResultCacheable());
            assertEquals("cached:source", entityService.findById(entity.getId()).block().getName());
            assertEquals("cached:source", entityService.getCacheAll().filter(row -> entity.getId().equals(row.getId()))
                .single().block().getName());
            assertEquals("cached:source", entityService.getCache().getMono("id:" + entity.getId()).block().getName());
            sql.reset();
            for (String view : List.of("scope-A", "scope-B")) {
                assertEquals("cached:source", entityService.findById(entity.getId())
                    .contextWrite(context -> context.put("view", view)).block().getName());
                assertEquals("cached:source", entityService.getCacheAll().filter(row -> entity.getId().equals(row.getId()))
                    .single().contextWrite(context -> context.put("view", view)).block().getName());
            }
            assertEquals(0, sql.count("select"));

            // 原写入失效必须同时移除 ID 和全量结果，重新读取时只转换一次。
            entityService.updateById(entity.getId(), TestEntity.of("updated", 20, "cache-pure")).block();
            sql.reset();
            assertEquals("cached:updated", entityService.findById(entity.getId()).block().getName());
            assertEquals("cached:updated", entityService.getCacheAll().filter(row -> entity.getId().equals(row.getId()))
                .single().block().getName());
            assertEquals(2, sql.count("select"));
        } finally {
            table.getFeatures().remove(ReactiveQueryResultTransformer.ID_VALUE);
            entityService.getCache().clear().block();
        }
    }

    @Test
    public void ordinaryCacheDslKeepsExistingClearAndAppendedHookBehavior() {
        for (boolean callerTransaction : List.of(false, true)) {
            for (String operation : List.of("update", "delete")) {
                TestEntity entity = TestEntity.of("cache-hook-before", 10, "cache marker");
                entityService.insert(entity).as(StepVerifier::create).expectNext(1).verifyComplete();
                String sentinel = "sentinel:" + IDGenerator.RANDOM.generate();
                entityService.getCache().put(sentinel, Mono.just(entity)).as(StepVerifier::create).verifyComplete();
                List<String> hooks = new ArrayList<>();
                sql.reset();
                cacheDsl(operation, entity.getId(), sentinel, false, hooks, callerTransaction)
                    .as(StepVerifier::create).expectNext(1).verifyComplete();
                assertEquals(List.of("before", "after:1"), hooks);
                assertEquals(1, sql.count(operation));
                if ("update".equals(operation)) {
                    entityService.getRepository().findById(entity.getId()).map(TestEntity::getName)
                        .as(StepVerifier::create).expectNext("cache-hook-after").verifyComplete();
                } else {
                    entityService.getRepository().findById(entity.getId()).as(StepVerifier::create).verifyComplete();
                }

                // 零修改不能清除已有缓存；后追加 hook 仍执行一次并收到零结果。
                entityService.getCache().put(sentinel, Mono.just(entity)).as(StepVerifier::create).verifyComplete();
                hooks.clear();
                sql.reset();
                cacheDsl(operation, IDGenerator.RANDOM.generate(), sentinel, true, hooks, callerTransaction)
                    .as(StepVerifier::create).expectNext(0).verifyComplete();
                assertEquals(List.of("before", "after:0"), hooks);
                assertEquals(1, sql.count(operation));
                entityService.getCache().getMono(sentinel).map(TestEntity::getId)
                    .as(StepVerifier::create).expectNext(entity.getId()).verifyComplete();
            }
        }
    }

    private Mono<Integer> cacheDsl(String operation, String id, String sentinel, boolean cacheRetained,
                                   List<String> hooks, boolean callerTransaction) {
        Function<Mono<Integer>, Mono<Integer>> observe = execution -> Mono.defer(() -> {
            hooks.add("before");
            return execution.flatMap(count -> entityService.getCache().getMono(sentinel).hasElement()
                .doOnNext(present -> assertEquals("cache hook must run before the appended hook", cacheRetained, present))
                .thenReturn(count).doOnNext(result -> hooks.add("after:" + result)));
        });
        Mono<Integer> execution = "update".equals(operation)
            ? entityService.createUpdate().set("name", "cache-hook-after").where("id", id)
                .onExecute((update, original) -> observe.apply(original)).execute()
            : entityService.createDelete().where("id", id)
                .onExecute((delete, original) -> observe.apply(original)).execute();
        return callerTransaction
            ? TransactionUtils.tryRunInTransaction(execution, new DefaultTransactionDefinition()) : execution;
    }

    @Test
    public void test() {

        TestEntity entity = TestEntity.of("test2",100,"testName");

        entityService.insert(Mono.just(entity))
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

        entityService.findById(Mono.just(entity.getId()))
                .map(TestEntity::getId)
                .as(StepVerifier::create)
                .expectNext(entity.getId())
                .verifyComplete();

        entityService.getCache()
                .getMono("id:".concat(entity.getId()))
                .map(TestEntity::getId)
                .as(StepVerifier::create)
                .expectNext(entity.getId())
                .verifyComplete();

        entityService.createUpdate()
                .set("age",120)
                .where("id",entity.getId())
                .execute()
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

        entityService.getCache()
                .getMono("id:".concat(entity.getId()))
                .switchIfEmpty(Mono.error(NullPointerException::new))
                .as(StepVerifier::create)
                .expectError(NullPointerException.class)
                .verify();


    }

    @Test
    public void test2() {

        TestEntity entity = TestEntity.of("test1",100,"testName");

        entityService
                .createDelete()
                .notNull(TestEntity::getId)
                .execute()
                .block();

        entityService
                .insert(Mono.just(entity))
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

        entityService
                .getCacheAll()
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        entity.setAge(120);
        entityService
                .updateById(entity.getId(), entity)
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

        entityService
                .getCacheAll()
                .switchIfEmpty(Mono.error(NullPointerException::new))
                .as(StepVerifier::create)
                .expectNextMatches(t -> t.getAge().equals(120))
                .verifyComplete();

        entity.setId(null);
        entityService
                .insert(Mono.just(entity))
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

        entityService
                .getCacheAll()
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();

        entityService
                .deleteById(entity.getId())
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        entityService
                .getCacheAll()
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

}
