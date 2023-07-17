package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.exception.ValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ReactiveTreeSortEntityServiceTest {

    @Autowired
    private TestTreeSortEntityService sortEntityService;


    @Test
    public void testCrud() {
        TestTreeSortEntity entity = new TestTreeSortEntity();
        entity.setId("test");
        entity.setName("test");

        TestTreeSortEntity entity2 = new TestTreeSortEntity();
        entity2.setName("test2");

        entity.setChildren(Arrays.asList(entity2));

        sortEntityService.insert(Mono.just(entity))
                         .as(StepVerifier::create)
                         .expectNext(2)
                         .verifyComplete();

        sortEntityService.save(Mono.just(entity))
                         .map(SaveResult::getTotal)
                         .as(StepVerifier::create)
                         .expectNext(2)
                         .verifyComplete();

        sortEntityService.queryResultToTree(QueryParamEntity.of())
                         .map(List::size)
                         .as(StepVerifier::create)
                         .expectNext(1)
                         .verifyComplete();

        sortEntityService.queryIncludeParent(Arrays.asList(entity2.getId()))
                         .as(StepVerifier::create)
                         .expectNextCount(2)
                         .verifyComplete();


        sortEntityService.deleteById(Mono.just("test"))
                         .as(StepVerifier::create)
                         .expectNext(2)
                         .verifyComplete();
    }

    @Test
    public void testChangeParent() {
        TestTreeSortEntity entity = new TestTreeSortEntity();
        entity.setId("test_p1");
        entity.setName("test1");

        TestTreeSortEntity entity_0 = new TestTreeSortEntity();
        entity_0.setId("test_p0");
        entity_0.setName("test0");

        TestTreeSortEntity entity2 = new TestTreeSortEntity();
        entity2.setId("test_p2");
        entity2.setName("test2");
        entity2.setParentId(entity.getId());

        TestTreeSortEntity entity3 = new TestTreeSortEntity();
        entity3.setId("test_p3");
        entity3.setName("test3");
        entity3.setParentId(entity2.getId());

        sortEntityService
                .save(Arrays.asList(entity, entity_0, entity2, entity3))
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        entity2.setChildren(null);
        entity2.setParentId(entity_0.getId());

        sortEntityService
                .save(Arrays.asList(entity2))
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        sortEntityService.queryIncludeChildren(Arrays.asList(entity_0.getId()))
                         .as(StepVerifier::create)
                         .expectNextCount(3)
                         .verifyComplete();

    }

    @Test
    public void testSave() {
        TestTreeSortEntity entity = new TestTreeSortEntity();
        entity.setId("test_path");
        entity.setName("test-path");

        sortEntityService
                .save(entity)
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
        String firstPath = entity.getPath();
        assertNotNull(firstPath);
        entity.setPath(null);

        sortEntityService
                .save(entity)
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        sortEntityService
                .findById(entity.getId())
                .map(TestTreeSortEntity::getPath)
                .as(StepVerifier::create)
                .expectNext(firstPath)
                .verifyComplete();
    }

    @Test
    public void testNotExistParentId(){
        TestTreeSortEntity entity = new TestTreeSortEntity();
        entity.setId("NotExistParentIdTest");
        entity.setName("NotExistParentIdTest");
        entity.setParentId("NotExistParentId");

        sortEntityService
                .save(entity)
                .then()
                .as(StepVerifier::create)
                .expectError(ValidationException.class)
                .verify();

        TestTreeSortEntity entity2 = new TestTreeSortEntity();
        entity2.setId("NotExistParentId");
        entity2.setName("NotExistParentId");

        sortEntityService
                .save(Flux.just(entity,entity2))
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }
}