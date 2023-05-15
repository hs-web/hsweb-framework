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
    public void testGroup() {
        DefaultQueryHelper helper = new DefaultQueryHelper(database);

        helper.select("select name,count(1) _total from s_test group by name having count(1) > ? ", 0)
              .where(dsl -> dsl
                      .is("age", "31"))
              .fetchPaged()
              .doOnNext(v -> System.out.println(JSON.toJSONString(v, SerializerFeature.PrettyFormat)))
              .as(StepVerifier::create)
              .expectNextCount(1)
              .verifyComplete();
    }

    @Test
    public void testNative() {
        database.dml()
                .insert("s_test_event")
                .value("id", "helper_testNative")
                .value("name", "Ename2")
                .execute()
                .sync();

        database.dml()
                .insert("s_test")
                .value("id", "helper_testNative")
                .value("name", "main2")
                .value("age", 20)
                .execute()
                .sync();

        DefaultQueryHelper helper = new DefaultQueryHelper(database);

        helper.select("select e.*,t.id as \"id\" from s_test t " +
                              "left join s_test_event e on e.id = t.id " +
                              "where t.age = ? order by t.age desc", 20)
              .where(dsl -> dsl
                      .is("e.id", "helper_testNative")
                      .is("t.age", "20"))
              .fetchPaged()
              .doOnNext(v -> System.out.println(JSON.toJSONString(v, SerializerFeature.PrettyFormat)))
              .as(StepVerifier::create)
              .expectNextCount(1)
              .verifyComplete();

        helper.select("select id,name from s_test t " +
                              "union all select id,name from s_test_event")
              .where(dsl -> dsl
                      .is("id", "helper_testNative"))
              .fetchPaged()
              .doOnNext(v -> System.out.println(JSON.toJSONString(v, SerializerFeature.PrettyFormat)))
              .as(StepVerifier::create)
              .expectNextCount(1)
              .verifyComplete();


    }

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
                .value("testName","testName")
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
                               .is("event.name", "Ename")
                               .orNest()
                               .is(TestEntity::getName, "main")
                               .is("e2.name", "Ename")
                               .end()
              )
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

        private String testName;

        private EventTestEntity event;
    }
}