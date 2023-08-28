package org.hswebframework.web.crud.query;

import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
class QueryAnalyzerImplTest {
    @Autowired
    private DatabaseOperator database;


    @Test
    void testInject() {
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(database,
                                                           "select count(distinct time) t2, \"name\" n from \"s_test\" t");
        SqlRequest request = analyzer.refactor(
                QueryParamEntity
                        .newQuery()
                        .and("name", "123")
                        .getParam());

        System.out.println(request);

        SqlRequest sql = analyzer.refactorCount(
                QueryParamEntity
                        .newQuery()
                        .and("name", "123")
                        .getParam());
        System.out.println(sql);

    }


    @Test
    void testUnion() {
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(database,
                                                           "select name n from s_test t " +
                                                                   "union select name n from s_test t");

        assertNotNull(analyzer.select().table.alias, "t");
        assertNotNull(analyzer.select().table.metadata.getName(), "s_test");

        assertNotNull(analyzer.select().getColumns().get("n"));

    }

    @Test
    void test() {
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(database,
                                                           "select name n from s_test t");

        assertNotNull(analyzer.select().table.alias, "t");
        assertNotNull(analyzer.select().table.metadata.getName(), "s_test");

        assertNotNull(analyzer.select().getColumns().get("n"));


    }

    @Test
    void testSub() {
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(database,
                                                           "select * from ( select distinct(name) as n from s_test ) t");

        assertEquals(analyzer.select().table.alias, "t");

        assertNotNull(analyzer.select().getColumns().get("n"));

        SqlRequest request = analyzer
                .refactor(QueryParamEntity
                                  .newQuery()
                                  .where("n", "123")
                                  .getParam());

        System.out.println(request);

        database.sql()
                .reactive()
                .select(request, ResultWrappers.map())
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    @Test
    void testJoin() {
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(
                database,
                "select *,t2.c from s_test t " +
                        "left join (select z.id id, count(1) c from s_test z) t2 on t2.id = t.id");

        SqlRequest request = analyzer
                .refactor(QueryParamEntity
                                  .of()
                                  .and("t2.c", "is", "xyz"));

        System.out.println(request);

    }

    @Test
    void testPrepare(){
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(
                database,
                "select * from (select substring(id,9) id from s_test where left(id,1) = ?) t");

        SqlRequest request = analyzer
                .refactor(QueryParamEntity.of(),33);

        System.out.println(request);
    }
}