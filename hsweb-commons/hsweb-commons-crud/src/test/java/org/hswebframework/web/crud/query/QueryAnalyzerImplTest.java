package org.hswebframework.web.crud.query;

import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.crud.TestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class QueryAnalyzerImplTest {
    @Autowired
    private DatabaseOperator database;


    @Test
    public void testInject() {
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
    public void testUnion() {
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(database,
                                                           "select name n from s_test t " +
                                                                   "union select name n from s_test t");

        assertNotNull(analyzer.select().table.alias, "t");
        assertNotNull(analyzer.select().table.metadata.getName(), "s_test");

        assertNotNull(analyzer.select().getColumns().get("n"));

    }

    @Test
    public  void test() {
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(database,
                                                           "select name n from s_test t");

        assertNotNull(analyzer.select().table.alias, "t");
        assertNotNull(analyzer.select().table.metadata.getName(), "s_test");

        assertNotNull(analyzer.select().getColumns().get("n"));


    }

    @Test
    public void testSub() {
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
    public void testJoin() {
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
    public void testPrepare(){
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(
                database,
                "select * from (select substring(id,9) id from s_test where left(id,1) = ?) t");

        SqlRequest request = analyzer
                .refactor(QueryParamEntity.of(),33);

        System.out.println(request);
    }

    @Test
    public void testWith(){

        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(
                database,
        "WITH RECURSIVE Tree AS (\n" +
                "\n" +
                "  SELECT id\n" +
                "  FROM s_test\n" +
                "  WHERE id = ? \n" +
                "\t\n" +
                "  UNION ALL\n" +
                "\t\n" +
                "  SELECT ai.id\n" +
                "  FROM s_test AS ai\n" +
                "  INNER JOIN Tree AS tr ON ai.id = tr.id\n" +
                ")\n" +
                "SELECT t1.id\n" +
                "FROM Tree AS t1;");

        SqlRequest request = analyzer
                .refactor(QueryParamEntity.of().and("id","eq","test"),1);

        System.out.println(request);
    }

    @Test
    public void testTableFunction(){
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(
            database,
            "select t.key from json_each_text('{\"name\":\"test\"}') t");

        SqlRequest request = analyzer
            .refactor(QueryParamEntity.of().and("key","like","test%"),1);
        System.out.println(request);
    }

    @Test
    public void testTableFunctionJoin(){
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(
            database,
            "select t1.*,t2.key from s_test t1 left join json_each_text('{\"name\":\"test\"}') t2 on t2.key='test' and t2.value='test1'");

        SqlRequest request = analyzer
            .refactor(QueryParamEntity.of().and("t2.key","like","test%"),1);
        System.out.println(request);
    }

    @Test
    public  void testValues(){
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(
            database,
            "select * from (values (1,2),(3,4)) t(\"a\",b)");

        SqlRequest request = analyzer
            .refactor(QueryParamEntity.of().and("a","eq",1),1);
        System.out.println(request);
    }

    @Test
   public void testLateralSubSelect(){
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(
            database,
            "select * from s_test t, lateral(select * from s_test where id = t.id) t2");

        SqlRequest request = analyzer
            .refactor(QueryParamEntity.of().and("t2.id","eq","test"),1);
        System.out.println(request);
    }

    @Test
    public void testParenthesisFrom(){
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(
            database,
            "select * from (s_test) t");

        SqlRequest request = analyzer
            .refactor(QueryParamEntity.of().and("t.id","eq","test"),1);
        System.out.println(request);
    }


    @Test
    public void testDistinct() {
        QueryAnalyzerImpl analyzer = new QueryAnalyzerImpl(
            database,
            "select distinct upper(t.id) v from s_test t group by t.name");

        SqlRequest request = analyzer
            .refactor(QueryParamEntity.of().and("t.id", "eq", "test"), 1);

        System.out.println(request);

        System.out.println(analyzer.refactorCount(QueryParamEntity.of()));
    }
}