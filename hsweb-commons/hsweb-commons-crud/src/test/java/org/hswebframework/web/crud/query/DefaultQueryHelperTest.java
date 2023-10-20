package org.hswebframework.web.crud.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hswebframework.ezorm.core.param.Sort;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.crud.entity.EventTestEntity;
import org.hswebframework.web.crud.entity.TestEntity;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
class DefaultQueryHelperTest {

    @Autowired
    private DatabaseOperator database;


    @Test
    public void testPage(){
        DefaultQueryHelper helper = new DefaultQueryHelper(database);

        database.dml()
                .insert("s_test")
                .value("id", "page-test")
                .value("name", "page")
                .value("age", 22)
                .execute()
                .sync();

        database.dml()
                .insert("s_test")
                .value("id", "page-test2")
                .value("name", "page")
                .value("age", 22)
                .execute()
                .sync();

        helper.select("select * from s_test")
                .where(dsl->{
                    dsl.doPaging(0,1);
                })
                .fetch()
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void testGroup() {
        DefaultQueryHelper helper = new DefaultQueryHelper(database);

        database.dml()
                .insert("s_test")
                .value("id", "group-test")
                .value("name", "group")
                .value("age", 31)
                .execute()
                .sync();

        helper.select("select name as \"name\",count(1) totalResult from s_test group by name having count(1) > ? ", GroupResult::new, 0)
//              .where(dsl -> dsl
//                      .is("age", "31")
//                      .orderByAsc(GroupResult::getTotalResult))
              .fetch()
              .doOnNext(v -> System.out.println(JSON.toJSONString(v, SerializerFeature.PrettyFormat)))
              .as(StepVerifier::create)
              .expectNextCount(1)
              .verifyComplete();
    }

    @Test
    public void testInner() {
        DefaultQueryHelper helper = new DefaultQueryHelper(database);

        database.dml()
                .insert("s_test")
                .value("id", "inner-test")
                .value("name", "inner")
                .value("testName", "inner")
                .value("age", 31)
                .execute()
                .sync();


        helper.select("select age,count(1) c from ( select *,'1' as x from s_test ) a group by age ", 0)
              .where(dsl -> dsl
                      .is("x", "1")
                      .is("name", "inner")
                      .is("a.testName", "inner")
                      .is("age", 31))
              .fetchPaged(0, 10)
              .doOnNext(v -> System.out.println(JSON.toJSONString(v, SerializerFeature.PrettyFormat)))
              .as(StepVerifier::create)
              .expectNextCount(1)
              .verifyComplete();
    }

    @Getter
    @Setter
    public static class GroupResult {
        private String name;
        private BigDecimal totalResult;
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
        QueryParamEntity param = QueryParamEntity
                .newQuery()
                .is("e.id", "helper_testNative")
                .is("t.age", "20")
                .orderByAsc("t.age")
                .getParam();

        {
            Sort sortByValue = new Sort();
            sortByValue.setName("t.id");
            sortByValue.setValue("1");
            param.getSorts().add(sortByValue);
        }
        {
            Sort sortByValue = new Sort();
            sortByValue.setName("t.id");
            sortByValue.setValue("2");
            param.getSorts().add(sortByValue);
        }


        helper.select("select t.*,e.*,e.name ename,e.id `x.id` from s_test t " +
                              "left join s_test_event e on e.id = t.id " +
                              "where t.age = ?", 20)
              .logger(LoggerFactory.getLogger("org.hswebframework.test.native"))
              .where(param)
              .fetchPaged()
              .doOnNext(v -> System.out.println(JSON.toJSONString(v, SerializerFeature.PrettyFormat)))
              .as(StepVerifier::create)
              .expectNextCount(1)
              .verifyComplete();

        helper.select("select id,name from s_test t " +
                              "union all select id,name from s_test_event")
              .where(dsl -> dsl
                      .is("id", "helper_testNative")
                      .orderByAsc("name"))
              .fetchPaged()
              .doOnNext(v -> System.out.println(JSON.toJSONString(v, SerializerFeature.PrettyFormat)))
              .as(StepVerifier::create)
              .expectNextCount(1)
              .verifyComplete();


    }

    @Test
    public void testCustomFirstPageIndex() {
        DefaultQueryHelper helper = new DefaultQueryHelper(database);
        QueryParamEntity e = new QueryParamEntity();
        e.and("id", "eq", "testCustomFirstPageIndex");
        e.setFirstPageIndex(1);
        e.setPageIndex(2);

        {
            helper.select(TestInfo.class)
                  .from(TestEntity.class)
                  .where(e)
                  .fetchPaged()
                  .doOnNext(info -> System.out.println(JSON.toJSONString(info, SerializerFeature.PrettyFormat)))
                  .as(StepVerifier::create)
                  .expectNextMatches(p -> p.getPageIndex() == 1)
                  .verifyComplete();
        }

        {
            helper.select("select * from s_test")
                  .where(e)
                  .fetchPaged()
                  .doOnNext(info -> System.out.println(JSON.toJSONString(info, SerializerFeature.PrettyFormat)))
                  .as(StepVerifier::create)
                  .expectNextMatches(p -> p.getPageIndex() == 1)
                  .verifyComplete();
        }
    }

    @Test
    public void test() {

        database.dml()
                .insert("s_test_event")
                .value("id", "helper_test")
                .value("name", "main")
                .value("age", 10)
                .execute()
                .sync();

        database.dml()
                .insert("s_test")
                .value("id", "helper_test")
                .value("name", "main")
                .value("testName", "testName")
                .value("age", 10)
                .execute()
                .sync();

        DefaultQueryHelper helper = new DefaultQueryHelper(database);

        helper.select(TestInfo.class)
              .all(EventTestEntity.class, TestInfo::setEventList)
//              .all("e2", TestInfo::setEvent)
              .all(TestEntity.class)
              .from(TestEntity.class)
              .leftJoin(EventTestEntity.class,
                        join -> join
                                .alias("e1")
                                .is(EventTestEntity::getId, TestEntity::getId)
//                                .is(EventTestEntity::getName, TestEntity::getId)
                                .notNull(EventTestEntity::getAge))
//              .leftJoin(EventTestEntity.class,
//                        join -> join
//                                .alias("e2")
//                                .is(EventTestEntity::getId, TestEntity::getId))

//              .where(dsl -> dsl.is(EventTestEntity::getName, "Ename")
//                               .is("e1.name", "Ename")
//                               .orNest()
//                               .is(TestEntity::getName, "main")
//                               .is("e1.name", "Ename")
//                               .end()
//              )
              .orderByAsc(TestEntity::getAge)
              .orderByDesc(EventTestEntity::getAge)
              .fetchPaged(0, 10)
              .doOnNext(info -> System.out.println(JSON.toJSONString(info, SerializerFeature.PrettyFormat)))
              .as(StepVerifier::create)
              .expectNextCount(1)
              .verifyComplete();

    }

    @Getter
    @Setter
    @ToString
    public static class TestInfo extends TestInfoSuper {

        private String id;

        private String name;

        private Integer age;

        private String testName;

        private EventTestEntity event;

    }

    @Getter
    @Setter
    public static class TestInfoSuper {
        private List<EventTestEntity> eventList;
    }
}