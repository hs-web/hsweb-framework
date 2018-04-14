package org.hswebframework.web.dao.crud;

import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

        query.where("dataTypes", "ain",Arrays.asList(DataType.TYPE4, DataType.TYPE2));

        List<TestEntity> entities = testDao.query(query);

        System.out.println(entities);
    }

}
