package org.hswebframework.web.utils;

import com.alibaba.fastjson.JSON;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;


public class ExpressionUtilsTest {

    @Test
    public void testArray() {
        String expression = ExpressionUtils.analytical("test-${array}", Collections.singletonMap("array", Arrays.asList(1,2,3)), "spel");

        Assert.assertEquals(expression,"test-[1, 2, 3]");
    }

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

    @Test
    public void testJson(){

       Assert.assertEquals("{\"name\":\"test\"}",TemplateParser.parse("{\"name\":\"${#name}\"}",e->{
            System.out.println(e);
            Assert.assertEquals(e,"#name");
            return "test";
        }));

    }

    @Test
    public void testJson2(){
        String js = ExpressionUtils.analytical("{\n" +
                "     \"msgtype\": \"markdown\",\n" +
                "     \"markdown\": {\n" +
                "         \"title\":\"消息类型:${messageType}\",\n" +
                "         \"text\": \" - 设备ID: `${deviceId}` \\n - 设备型号: `${headers.productId}`\\n - 设备名称: `${headers.deviceName}`\"" +
                "     \n},\n" +
                "      \"at\": {\n" +
                "          \"isAtAll\": false\n" +
                "      }\n" +
                "}", JSON.parseObject("{\n" +
                "  \"deviceId\": \"VIS-Mandrake-12289\",\n" +
                "  \"headers\": {\n" +
                "    \"productId\": \"VIS-Mandrake\",\n" +
                "    \"deviceName\": \"能见度仪-曼德克-01\"\n" +
                "  },\n" +
                "  \"messageType\": \"OFFLINE\",\n" +
                "  \"timestamp\": 1592098397277\n" +
                "}"), "spel");

        System.out.println(js);
    }
}