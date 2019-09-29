package org.hswebframework.web.test.init;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.test.TestApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = TestApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class InitTest  {

    @Autowired
    private DatabaseOperator operator;


    @Test
    public void test(){
        Assert.assertNotNull(operator);
    }
}
