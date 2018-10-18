package org.hswebframework.web.service.file;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * fix bug #93 test
 *
 * @author zhouhao
 * @since 3.0.2
 */
public class FixBug93Test extends SimpleWebApplicationTests {

    @Test
    public void testUploadFile() throws Exception {
        String result = mvc.perform(
                MockMvcRequestBuilders
                        .fileUpload("/file/upload")
                        .file(new MockMultipartFile("file", "test中文.txt", MediaType.TEXT_PLAIN_VALUE, "test".getBytes()))
        ).andReturn()
                .getResponse()
                .getContentAsString();

        Assert.assertEquals(JSON.parseObject(result).getJSONObject("result").getString("name"), "test中文.txt");
    }

}
