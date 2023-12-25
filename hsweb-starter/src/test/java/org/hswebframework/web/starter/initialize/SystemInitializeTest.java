package org.hswebframework.web.starter.initialize;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class SystemInitializeTest {


    @Autowired
    DatabaseOperator databaseOperator;

    @Test
    public void test(){
        assertNotNull(databaseOperator);
    }

}