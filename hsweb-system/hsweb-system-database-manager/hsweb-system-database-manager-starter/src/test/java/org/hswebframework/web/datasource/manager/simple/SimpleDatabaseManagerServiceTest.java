package org.hswebframework.web.datasource.manager.simple;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.database.manager.DatabaseManagerService;
import org.hswebframework.web.database.manager.SqlExecuteRequest;
import org.hswebframework.web.database.manager.SqlExecuteResult;
import org.hswebframework.web.database.manager.SqlInfo;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleDatabaseManagerServiceTest extends SimpleWebApplicationTests {

    @Autowired
    private DatabaseManagerService databaseManagerService;

    @Test
    public void testExecuteSql() throws Exception {
        String id = databaseManagerService.newTransaction();
        SqlExecuteRequest request = new SqlExecuteRequest();
        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.setSql("create table t_test(name varchar(32))");
        sqlInfo.setType("create");

        SqlInfo sqlInfo2 = new SqlInfo();
        sqlInfo2.setSql("insert into t_test values('1234') ");
        sqlInfo2.setType("insert");

        request.setSql(Arrays.asList(sqlInfo));
        List<SqlExecuteResult> results = databaseManagerService.execute(id, request);
//        System.out.println(JSON.toJSONString(results));
        Assert.assertFalse(results.isEmpty());
        request.setSql(Arrays.asList(sqlInfo2));
        int total = 10;
        CountDownLatch countDownLatch = new CountDownLatch(total);

        for (int i = 0; i < total; i++) {
            new Thread(() -> {
                try {
                    databaseManagerService.execute(id, request);
                    Thread.sleep(100);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await(30, TimeUnit.SECONDS);

        sqlInfo = new SqlInfo();
        sqlInfo.setSql("select *,name as \"NAME\",1 as \"\" from t_test ");
        sqlInfo.setType("select");

        request.setSql(Arrays.asList(sqlInfo));
        results = databaseManagerService.execute(id, request);
        Assert.assertFalse(results.isEmpty());

//        System.out.println(JSON.toJSONString(results));

        Assert.assertTrue(sqlExecutor.list("select * from t_test").isEmpty());

        databaseManagerService.rollback(id);
        Assert.assertTrue(sqlExecutor.list("select * from t_test").isEmpty());


    }
}