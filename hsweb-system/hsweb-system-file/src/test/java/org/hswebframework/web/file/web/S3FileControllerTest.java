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

@WebFluxTest(S3FileController.class)
@RunWith(SpringRunner.class)
@ImportAutoConfiguration(S3FileStorageConfiguration.class)
public class S3FileControllerTest {

    static {
        System.setProperty("oss.s3.endpoint", "https://oss-cn-beijing.aliyuncs.com");
        System.setProperty("oss.s3.region", "us-east-1");
        System.setProperty("oss.s3.accessKey", "");
        System.setProperty("oss.s3.secretKey", "");
        System.setProperty("oss.s3.bucket", "maydaysansan");
        System.setProperty("file.storage", "s3");
    }

    @Autowired
    WebTestClient client;

    @Test
    public void testStatic(){
        client.post()
                .uri("/ossFile/static")
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
                        uriBuilder.path("/oss/file/stream")
                                .queryParam("fileType", "json")
                                .build())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(fileBytes)
                .exchange()
                .expectStatus().isOk();
    }

}