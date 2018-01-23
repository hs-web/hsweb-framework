package org.hswebframework.web.authorization.oauth2.client.simple.provider;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.authorization.oauth2.client.exception.OAuth2RequestException;
import org.hswebframework.web.authorization.simple.SimpleUser;
import org.hswebframework.web.authorization.simple.builder.SimpleAuthenticationBuilderFactory;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since
 */
public class HswebResponseConvertSupportTests {

    private HswebResponseConvertSupport convertSupport = new HswebResponseConvertSupport(new SimpleAuthenticationBuilderFactory(new SimpleDataAccessConfigBuilderFactory()));


    @Test
    public void testConvertSpringError() {
        String str = "{\"exception\":\"java.lang.RuntimeException\",\"path\":\"/file/upload-static\",\"error\":\"Internal Server Error\",\"message\":\"java.nio.file.FileSystemException: /tmp/undertow8543459739529410666upload: No space left on device\",\"timestamp\":\"2018-01-09 20:22:07\",\"status\":500}";
        try {
            convertSupport.convert(new MockOAuth2Response(str), String.class);
            Assert.assertTrue(false);
        } catch (OAuth2RequestException e) {
            // is ok
        }
    }

    @Test
    public void testConvertHswebError() {
        String str = ResponseMessage.error("test").toString();
        try {
            convertSupport.convert(new MockOAuth2Response(str), String.class);
            Assert.assertTrue(false);
        } catch (BusinessException e) {
            // is ok
        }
    }

    @Test
    public void testConvertHswebResponse() {
        String str = ResponseMessage.ok("test").toString();
        String res = convertSupport.convert(new MockOAuth2Response(str), String.class);
        Assert.assertEquals(res, "test");

        str = ResponseMessage.ok(1).toString();
        Assert.assertEquals((Object) convertSupport.convert(new MockOAuth2Response(str), Integer.class), 1);

        str = ResponseMessage.ok(true).toString();
        Assert.assertTrue(convertSupport.convert(new MockOAuth2Response(str), Boolean.class));

        str = ResponseMessage.ok("999999999999999999").toString();
        Assert.assertEquals(convertSupport.convert(new MockOAuth2Response(str), BigDecimal.class), new BigDecimal("999999999999999999"));

        SimpleUser user = SimpleUser.builder()
                .id("test").name("test").type("test").username("test")
                .build();

        str = ResponseMessage.ok(user).toString();
        SimpleUser resp = convertSupport.convert(new MockOAuth2Response(str), SimpleUser.class);

        Assert.assertEquals(JSON.toJSON(user), JSON.toJSON(resp));


    }

}