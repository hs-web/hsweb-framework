package org.hswebframework.web.file.web;

import org.hswebframework.web.file.S3FileStorageConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.BodyInserters;

@WebFluxTest(ReactiveFileController.class)
@RunWith(SpringRunner.class)
@ImportAutoConfiguration(S3FileStorageConfiguration.class)
public class OssUploadTest {

    static {
        System.setProperty("hsweb.file.upload.s3.endpoint", "https://oss-cn-beijing.aliyuncs.com");
        System.setProperty("hsweb.file.upload.s3.region", "us-east-1");
        System.setProperty("hsweb.file.upload.s3.accessKey", "");
        System.setProperty("hsweb.file.upload.s3.secretKey", "");
        System.setProperty("hsweb.file.upload.s3.bucket", "maydaysansan");
        System.setProperty("hsweb.file.storage", "s3");
    }

    @Autowired
    WebTestClient client;

    @Test
    public void testStatic(){
        client.post()
                .uri("/file/oss/static")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file",new HttpEntity<>(new ClassPathResource("test.json"))))
                .exchange()
                .expectStatus()
                .isOk();

    }

    @Test
    public void testStream() throws Exception {
        byte[] fileBytes = StreamUtils.copyToByteArray(new ClassPathResource("test.json").getInputStream());

        client.post()
                .uri(uriBuilder ->
                        uriBuilder.path("/file/oss/stream")
                                .queryParam("fileType", "json")
                                .build())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(fileBytes)
                .exchange()
                .expectStatus().isOk();
    }

}