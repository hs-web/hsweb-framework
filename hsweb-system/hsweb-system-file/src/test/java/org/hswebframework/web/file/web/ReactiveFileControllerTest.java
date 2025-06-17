package org.hswebframework.web.file.web;

import org.hswebframework.web.file.FileServiceConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import static org.junit.Assert.*;

@WebFluxTest(ReactiveFileController.class)
@RunWith(SpringRunner.class)
@ImportAutoConfiguration(FileServiceConfiguration.class)
public class ReactiveFileControllerTest {

    static {
        System.setProperty("hsweb.file.upload.static-file-path","./target/upload");
        System.setProperty("hsweb.file.storage","local");
//        System.setProperty("hsweb.file.upload.use-original-file-name","true");
    }

    @Autowired
    WebTestClient client;

    @Test
    public void test(){
       client.post()
                .uri("/file/static")
               .contentType(MediaType.MULTIPART_FORM_DATA)
               .body(BodyInserters.fromMultipartData("file",new HttpEntity<>(new ClassPathResource("test.json"))))
               .exchange()
                .expectStatus()
               .isOk();

    }
}