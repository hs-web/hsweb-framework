package org.hswebframework.web;

import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class ExpressionUtilsTests {

    @Test
    public void testAnalytical() throws Exception {
        String result = ExpressionUtils.analytical("test${1+2} ${''} ${1+4+5}", "spel");

        Assert.assertEquals(result, "test3  10");

        result = ExpressionUtils.analytical("test${#param}", Collections.singletonMap("param", "3"), "spel");
        Assert.assertEquals(result, "test3");
    }

    @Test
    @SneakyThrows
    public void benchmark() {

        String expression = "test${1+2}  ${1+4+5}";

        ExpressionUtils.analytical(expression, Collections.emptyMap(), "spel");

        long time = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            ExpressionUtils.analytical(expression, Collections.emptyMap(), "spel");
        }

        System.out.println(System.currentTimeMillis() - time);
    }


}