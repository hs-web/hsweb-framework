package org.hswebframework.web;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class SqlsTests {

    @Test
    public void testParse() {
        String sql = "select 1;\ndelete from user;";

        List<String> strings = Sqls.parse(sql);
        System.out.println(strings);
        Assert.assertTrue(strings.size() == 2);

        Assert.assertTrue("select 1".equals(strings.get(0)));
        Assert.assertTrue("delete from user".equals(strings.get(1)));


        sql = "select 1;\ndelete from user;\nselect * from user \nwhere name = 1 \n or name =2";

       strings = Sqls.parse(sql);
        System.out.println(strings);
        Assert.assertTrue(strings.size() == 3);
        Assert.assertEquals(strings.get(2),"select * from user \nwhere name = 1 \n or name =2");


    }
}