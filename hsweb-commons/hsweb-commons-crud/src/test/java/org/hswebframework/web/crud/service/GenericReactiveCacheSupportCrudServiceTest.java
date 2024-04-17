package org.hswebframework.web.crud.service;

import org.hswebframework.web.crud.TestApplication;
import org.hswebframework.web.crud.entity.TestEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(classes = TestApplication.class, args = "--hsweb.cache.type=guava")
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class GenericReactiveCacheSupportCrudServiceTest {

    @Autowired
    private TestCacheEntityService entityService;

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