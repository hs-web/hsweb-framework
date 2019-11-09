package org.hswebframework.web.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class TemplateParserTest {


    @Test
    public void test() {

        String result = TemplateParser.parse("test-${name}-${name}", Collections.singletonMap("name", "test"));

        Assert.assertEquals(result, "test-test-test");
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


}