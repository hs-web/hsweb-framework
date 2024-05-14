package org.hswebframework.web.crud.events;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.NativeSql;
import org.hswebframework.web.crud.TestApplication;
import org.hswebframework.web.crud.entity.EventTestEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.annotation.PostConstruct;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class EntityEventListenerTest {

    @Autowired
    private ReactiveRepository<EventTestEntity, String> reactiveRepository;

    @Autowired
    private TransactionalOperator transactionalOperator;

    @Autowired
    private TestEntityListener listener;

    @Before
    public void before(){
        listener.reset();
    }
    @Test
    public void test() {
        Mono.just(EventTestEntity.of("test", 1))
            .as(reactiveRepository::insert)
            .as(StepVerifier::create)
            .expectNext(1)
            .verifyComplete();
        Assert.assertEquals(listener.created.getAndSet(0), 1);


    }

    @Test
    public void testPrepareModify() {
        EventTestEntity entity = EventTestEntity.of("prepare", 10);
        reactiveRepository
            .insert(entity)
            .as(StepVerifier::create)
            .expectNext(1)
            .verifyComplete();
        Assert.assertEquals(listener.created.getAndSet(0), 1);

        reactiveRepository
            .createUpdate()
            .set("name","prepare-xx")
            .set("age",20)
            .where("id",entity.getId())
            .execute()
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();

        reactiveRepository
            .findById(entity.getId())
            .map(EventTestEntity::getName)
            .as(StepVerifier::create)
            .expectNext("prepare-0")
            .verifyComplete();

    }

    @Test
    public void testUpdateNative() {
        EventTestEntity entity = EventTestEntity.of("test-update-native", null);
        reactiveRepository
            .insert(entity)
            .as(StepVerifier::create)
            .expectNext(1)
            .verifyComplete();
        Assert.assertEquals(listener.created.getAndSet(0), 1);

        reactiveRepository
            .createUpdate()
            .set(EventTestEntity::getAge, NativeSql.of("coalesce(age+1,?)", 10))
            .where()
            .is(entity::getName)
            .execute()
            .as(StepVerifier::create)
            .expectNext(1)
            .verifyComplete();

        Assert.assertEquals(1, listener.modified.getAndSet(0));

    }

    @Test
    public void testInsertBatch() {
        reactiveRepository.createQuery()
                          .where(EventTestEntity::getId, "test")
                          .fetch()
                          .then()
                          .as(StepVerifier::create)
                          .expectComplete()
                          .verify();
        Assert.assertEquals(listener.beforeQuery.getAndSet(0), 1);


        Flux.just(EventTestEntity.of("test2", 1), EventTestEntity.of("test3", 2))
            .as(reactiveRepository::insert)
            .as(StepVerifier::create)
            .expectNext(2)
            .verifyComplete();
        Assert.assertEquals(listener.created.getAndSet(0), 2);
        Assert.assertEquals(listener.beforeCreate.getAndSet(0), 2);

        reactiveRepository
            .createUpdate().set("age", 3).where().in("name", "test2", "test3").execute()
            .as(StepVerifier::create)
            .expectNext(2)
            .verifyComplete();

        Assert.assertEquals(listener.modified.getAndSet(0), 2);
        Assert.assertEquals(listener.beforeModify.getAndSet(0), 2);

        reactiveRepository.createDelete().where().in("name", "test2", "test3").execute()
                          .as(StepVerifier::create)
                          .expectNext(2)
                          .verifyComplete();

        Assert.assertEquals(listener.deleted.getAndSet(0), 2);
        Assert.assertEquals(listener.beforeDelete.getAndSet(0), 2);

        reactiveRepository.save(EventTestEntity.of("test2", 1))
                          .then()
                          .as(StepVerifier::create)
                          .expectComplete()
                          .verify();

        Assert.assertEquals(listener.saved.getAndSet(0), 1);
        Assert.assertEquals(listener.beforeSave.getAndSet(0), 1);


    }

    @Test
    @Ignore
    public void testInsertError() {
        Flux.just(EventTestEntity.of("test2", 1), EventTestEntity.of("test3", 2))
            .as(reactiveRepository::insert)
            .flatMap(i -> Mono.error(new RuntimeException()))
            .as(transactionalOperator::transactional)
            .as(StepVerifier::create)
            .verifyError();

        Assert.assertEquals(listener.created.getAndSet(0), 0);
    }


    @Test
    public void testDoNotFire() {
        Mono.just(EventTestEntity.of("test", 1))
            .as(reactiveRepository::insert)
            .as(EntityEventHelper::setDoNotFireEvent)
            .as(StepVerifier::create)
            .expectNext(1)
            .verifyComplete();
        Assert.assertEquals(listener.created.getAndSet(0), 0);


    }

}