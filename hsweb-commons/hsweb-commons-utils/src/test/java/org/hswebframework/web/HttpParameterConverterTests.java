package org.hswebframework.web;

import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class HttpParameterConverterTests {

    @Test
    public void testConvertMap() {
        Map<String, Object> target = new HashMap<>();

        Map<String, Object> info = new HashMap<>();
        info.put("nickName", "小宋");
        info.put("address", "重庆");


        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("lastLoginIp", "127.0.0.1");
        loginInfo.put("lastLoginTime", new Date());
        loginInfo.put("lastLoginIp5Times", Arrays.asList("127.0.0.1", "localhost"));

        info.put("loginInfo", loginInfo);

        target.put("name", "admin");
        target.put("age", 30);
        target.put("money", new BigDecimal("1000000.00"));
        target.put("createDate", new Date());
        target.put("roles", Arrays.asList(1, 2, 3));
        target.put("info", info);

        HttpParameterConverter converter = new HttpParameterConverter(target);

        Map<String, String> result = converter.convert();

        System.out.println(result);

        Assert.assertEquals(result.get("roles[0]"), "1");
        Assert.assertEquals(result.get("roles[1]"), "2");
        Assert.assertEquals(result.get("roles[2]"), "3");
        Assert.assertEquals(result.get("name"), "admin");
        Assert.assertEquals(result.get("info.nickName"), "小宋");
        Assert.assertEquals(result.get("info.address"), "重庆");

        Assert.assertEquals(result.get("info.loginInfo.lastLoginIp"), "127.0.0.1");
        Assert.assertEquals(result.get("info.loginInfo.lastLoginIp5Times[0]"), "127.0.0.1");
        Assert.assertEquals(result.get("info.loginInfo.lastLoginIp5Times[1]"), "localhost");

    }

    @Test
    public void testConvertObject() {
        QueryParam param = Query.empty(new QueryParam())
                .where("name", "张三")
                .and().like("address", "%重庆%")
                .nest()
                .lt("age", 18)
                .or()
                .gt("age", 60)
                .end()
                .getParam();

        HttpParameterConverter converter = new HttpParameterConverter(param);

        Map<String, String> result = converter.convert();

        System.out.println(result);

        Assert.assertEquals(result.get("terms[0].column"), "name");
        Assert.assertEquals(result.get("terms[0].value"), "张三");

        Assert.assertEquals(result.get("terms[1].termType"), "like");
        Assert.assertEquals(result.get("terms[1].value"), "%重庆%");

        Assert.assertEquals(result.get("terms[2].terms[0].termType"), "lt");
        Assert.assertEquals(result.get("terms[2].terms[0].value"), "18");

        Assert.assertEquals(result.get("terms[1].value"), "%重庆%");

    }
}