package org.hswebframework.web;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class ExpressionUtilsTests {

    @Test
    public void testAnalytical() throws Exception {
        String result = ExpressionUtils.analytical("test${1+2}", "spel");

        Assert.assertEquals(result, "test3");

        result = ExpressionUtils.analytical("test${#param}", Collections.singletonMap("param", "3"), "spel");
        Assert.assertEquals(result, "test3");
    }
}