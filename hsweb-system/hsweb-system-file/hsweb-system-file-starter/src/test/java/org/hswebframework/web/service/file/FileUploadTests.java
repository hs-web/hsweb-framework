package org.hswebframework.web.service.file;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class FileUploadTests extends SimpleWebApplicationTests {

    @Test
    public void testUploadFile() throws Exception {

        //test multi file upload
        String result = mvc.perform(
                MockMvcRequestBuilders
                        .fileUpload("/file/upload-multi")
                        .file(new MockMultipartFile("files", "test.txt",
                                MediaType.TEXT_PLAIN_VALUE, "test".getBytes()))
                        .file(new MockMultipartFile("files", "test2.txt",
                                MediaType.TEXT_PLAIN_VALUE, "test2".getBytes()))
        ).andReturn()
                .getResponse()
                .getContentAsString();


        Assert.assertEquals(JSON.parseObject(result).getJSONArray("result").size(), 2);
        System.out.println(result);
        String fileId = JSON.parseObject(result)
                .getJSONArray("result")
                .getJSONObject(0)
                .getString("id");
        String fileMd5 = JSON.parseObject(result)
                .getJSONArray("result")
                .getJSONObject(0)
                .getString("md5");

        result = testGet("/file/md5/" + fileMd5).exec().resultAsString();
        System.out.println(result);

        result = mvc.perform(MockMvcRequestBuilders.get("/file/download/" + fileId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println(result);

        result = mvc.perform(MockMvcRequestBuilders
                .fileUpload("/file/upload-static")
                .file(new MockMultipartFile("file", "test.txt",
                        MediaType.TEXT_PLAIN_VALUE, "test".getBytes())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println(result);
        Assert.assertNotNull(JSON.parseObject(result).getString("result"));


    }

}