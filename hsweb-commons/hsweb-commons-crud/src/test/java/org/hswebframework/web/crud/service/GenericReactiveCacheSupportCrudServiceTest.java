package org.hswebframework.web.crud.service;

import org.hswebframework.web.cache.ReactiveCacheManager;
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

import static org.junit.Assert.*;

@SpringBootTest(classes = TestApplication.class, args = "--hsweb.cache.type=guava")
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class GenericReactiveCacheSupportCrudServiceTest {

    @Autowired
    private TestCacheEntityService entityService;

    @Test
    public void test() {

        TestEntity entity = TestEntity.of("test2",100);

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

}