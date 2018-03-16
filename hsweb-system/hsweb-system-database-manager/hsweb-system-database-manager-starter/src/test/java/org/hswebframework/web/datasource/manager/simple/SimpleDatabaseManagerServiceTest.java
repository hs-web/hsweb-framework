package org.hswebframework.web.datasource.manager.simple;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.database.manager.DatabaseManagerService;
import org.hswebframework.web.database.manager.SqlExecuteRequest;
import org.hswebframework.web.database.manager.SqlExecuteResult;
import org.hswebframework.web.database.manager.SqlInfo;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
        System.out.println(JSON.toJSONString(results));
        request.setSql(Arrays.asList(sqlInfo2));
        int total = 1000;
        CountDownLatch countDownLatch = new CountDownLatch(total);

        for (int i = 0; i < total; i++) {
            new Thread(() -> {
                try {
                    databaseManagerService.execute(id, request);
                } catch (Exception e) {
                    throw new RuntimeException();
                }
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();

        sqlInfo = new SqlInfo();
        sqlInfo.setSql("select *,name as \"NAME\",1 as \"\" from t_test ");
        sqlInfo.setType("select");

        request.setSql(Arrays.asList(sqlInfo));
        results = databaseManagerService.execute(id, request);
        System.out.println(JSON.toJSONString(results));

        System.out.println(sqlExecutor.list("select * from t_test"));

        databaseManagerService.rollback(id);
        Thread.sleep(2000);
        System.out.println(sqlExecutor.list("select * from t_test").size());

    }
}