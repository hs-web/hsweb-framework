package org.hswebframework.web.dao.crud;

import org.apache.ibatis.session.SqlSessionFactory;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.dict.EnumDict;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class TestCrud extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private TestDao testDao;

    @Autowired
    private SqlExecutor sqlExecutor;


    @Autowired
    @Qualifier("sqlSessionFactory2")
    SqlSessionFactory sqlSessionFactory2;

    @Autowired
    @Qualifier("sqlSessionFactory")
    SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws SQLException {
        sqlExecutor.exec("\n" +
                "create table h_test(\n" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                "  name VARCHAR(32) ,\n" +
                "  create_time DATETIME,\n" +
                "  data_type SMALLINT,\n" +
                "  data_types BIGINT\n" +
                ")");
        sqlExecutor.exec("\n" +
                "create table h_nest_table(\n" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                "  name VARCHAR(32)\n" +
                ")");
    }

    @Test
    public void testInsert() {

        TestEntity entity = new TestEntity();
        entity.setName("测试");
        entity.setDataType(DataType.TYPE1);
        entity.setDataTypes(new DataType[]{DataType.TYPE1, DataType.TYPE3});
        testDao.insert(entity);
        Assert.assertNotNull(entity.getId());

        QueryParamEntity query = new QueryParamEntity();
        //any in
        query.where("dataTypes$in$any", Arrays.asList(DataType.TYPE1, DataType.TYPE2));
        query.includes("nest.name", "*");

        //  DataSourceHolder.tableSwitcher().use("h_test", "h_test2");
        List<TestEntity> entities = testDao.queryNest(query);

//        testDao.query(entity);

        System.out.println(entities);
    }


}
