package org.hswebframework.web;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class WebUtilTest {

    @Test
    public void queryStringToMap() throws UnsupportedEncodingException {
        String parameter = "key1=value1&key2=value2&key3=&key4=key5=1";
        Map<String, String> map = WebUtil.queryStringToMap(parameter, "utf-8");
        System.out.println(map);
        Assert.assertEquals(map.get("key1"), "value1");
        Assert.assertEquals(map.get("key2"), "value2");
        Assert.assertEquals(map.get("key3"), "");
        Assert.assertEquals(map.get("key4"), "key5=1");

        parameter = "key1=%e5%80%bc1&key2=%e5%80%bc2";
        map = WebUtil.queryStringToMap(parameter, "utf-8");
        System.out.println(map);
        Assert.assertEquals(map.get("key1"), "值1");
        Assert.assertEquals(map.get("key2"), "值2");

        parameter = "key1=%D6%B51&key2=%D6%B52";
        map = WebUtil.queryStringToMap(parameter, "gbk");
        System.out.println(map);
        Assert.assertEquals(map.get("key1"), "值1");
        Assert.assertEquals(map.get("key2"), "值2");

    }

}