package org.hswebframework.web.crud;

import org.hswebframework.web.crud.entity.CustomTestEntity;
import org.hswebframework.web.crud.entity.TestEntity;
import org.hswebframework.web.crud.events.EntityBeforeModifyEvent;
import org.hswebframework.web.crud.service.CustomTestCustom;
import org.hswebframework.web.crud.service.TestEntityService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(classes = {TestApplication.class,TestEntityService.class, CustomTestCustom.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class CrudTests {

    @Autowired
    private TestEntityService service;


    @Test
    public void test() {

        CustomTestEntity entity = new CustomTestEntity();
        entity.setExt("xxx");
        entity.setAge(1);
        entity.setName("test");

        entity.setExtension("extName", "test");

        service.insert(entity)
               .as(StepVerifier::create)
               .expectNext(1)
               .verifyComplete();
        Assert.assertNotNull(entity.getId());

        service.findById(entity.getId())
               .doOnNext(System.out::println)
               .as(StepVerifier::create)
               .expectNextMatches(e -> e instanceof CustomTestEntity)
               .verifyComplete();

        service.createUpdate()
               .set("name", "test2")
               .where("id", entity.getId())
               .execute()
               .as(StepVerifier::create)
               .expectNext(1)
               .verifyComplete();
    }
}
