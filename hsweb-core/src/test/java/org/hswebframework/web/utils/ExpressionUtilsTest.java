package org.hswebframework.web.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;


public class ExpressionUtilsTest {


    @Test
    public void test() {
        String expression = ExpressionUtils.analytical("test-${#name}", Collections.singletonMap("name", "test"), "spel");

        Assert.assertEquals(expression,"test-test");

        String res = ExpressionUtils.analytical("test-${3+2}", Collections.singletonMap("name", "test"), "spel");

        Assert.assertEquals(res,"test-5");
    }

    @Test
    public void testComplete(){

        TemplateParser.parse("${#data[payload][a_name]} ${#data[payload][b_name]} 发生 ${#data[payload][alarm_type_name]}",e->{
            System.out.println(e);
            return e;
        });

    }
}