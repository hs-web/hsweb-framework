package org.hswebframework.web.crud.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.crud.entity.EventTestEntity;
import org.hswebframework.web.crud.entity.TestEntity;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
class DefaultQueryHelperTest {

    @Autowired
    private DatabaseOperator database;


    @Test
    public void test() {

        database.dml()
                .insert("s_test_event")
                .value("id", "helper_test")
                .value("name", "Ename")
                .execute()
                .sync();

        database.dml()
                .insert("s_test")
                .value("id", "helper_test")
                .value("name", "main")
                .value("age", 10)
                .execute()
                .sync();

        DefaultQueryHelper helper = new DefaultQueryHelper(database);

        helper.select(TestInfo.class)
              .all(EventTestEntity.class, TestInfo::setEvent)
              .all(TestEntity.class)
              .from(TestEntity.class)
              .leftJoin(EventTestEntity.class,
                        join -> join
                                .alias("e1")
                                .is(EventTestEntity::getId, TestEntity::getId))
              .leftJoin(EventTestEntity.class,
                        join -> join
                                .alias("e2")
                                .is(EventTestEntity::getId, "e1", EventTestEntity::getId)
                                .nest()
                                .is(TestEntity::getId, EventTestEntity::getId))

              .where(dsl -> dsl.is(EventTestEntity::getName, "Ename")
                               .orNest()
                               .is(TestEntity::getName, "main"))
              .orderByAsc(TestEntity::getAge)
              .orderByDesc(EventTestEntity::getAge)
              .fetch()
              .doOnNext(info -> System.out.println(JSON.toJSONString(info, SerializerFeature.PrettyFormat)))
              .as(StepVerifier::create)
              .expectNextCount(1)
              .verifyComplete();

    }

    @Getter
    @Setter
    @ToString
    public static class TestInfo {

        private String id;

        private String name;

        private Integer age;

        private EventTestEntity event;
    }
}