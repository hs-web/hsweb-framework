package org.hswebframework.web.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.function.Function;

import static org.junit.Assert.*;

public class TemplateParserTest {


    @Test
    public void test() {

        String result = TemplateParser.parse("test-${name}-${name}", Collections.singletonMap("name", "test"));

        Assert.assertEquals(result, "test-test-test");
    }

    @Test
    public void testLargeExpr() {
        String expr = "";
        for (int i = 0; i < 1000; i++) {
            expr += "expr_" + i;
        }
        String result = TemplateParser.parse("${"+expr+"}", Function.identity());

        assertEquals(expr,result);

    }
    @Test
    public void testLarge() {
        String str = "";
        for (int i = 0; i < 1000; i++) {
            str += "test-" + i;
        }
        String result = TemplateParser.parse("test-${name}", Collections.singletonMap("name", str));

        Assert.assertEquals(result, "test-" + str);
    }


    @Test
    public void testNest() {


        String result = TemplateParser.parse("test-${properties.a-r-str}", Collections.singletonMap("properties", Collections.singletonMap("a-r-str","123")));

        Assert.assertEquals(result, "test-123");
    }

}