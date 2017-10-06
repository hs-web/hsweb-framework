package org.hswebframework.web.workflow.flowable;


import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO 完善rest测试
 *
 * @Author wangwei
 * @Date 2017/8/1.
 */
public class ControllerTest extends SimpleWebApplicationTests {

    @Test
    public void testRest() throws Exception {
        JSONObject res = testPost("/script").setUp(setup -> {
            setup.contentType(MediaType.APPLICATION_JSON);
            setup.content("{\"id\":\"test\",\"name\":\"test\",\"language\":\"js\",\"script\":\"return 'success';\"}");
        }).exec().resultAsJson();

        JSONObject jsonObject = testGet("/script/test/execute")
                .exec().resultAsJson();
        System.out.println(jsonObject);
        Assert.assertEquals(jsonObject.get("result"), "success");
    }

}
