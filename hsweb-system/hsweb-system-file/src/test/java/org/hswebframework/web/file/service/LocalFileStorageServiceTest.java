package org.hswebframework.web.file.service;

import lombok.SneakyThrows;
import org.hswebframework.web.file.FileUploadProperties;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class LocalFileStorageServiceTest {

    @Test
    @SneakyThrows
    public void test() {
        FileUploadProperties properties = new FileUploadProperties();
        properties.setStaticFilePath("./target/upload");
        properties.setStaticLocation("./");
        LocalFileStorageService storageService = new LocalFileStorageService(properties);

        String text = StreamUtils.copyToString(new ClassPathResource("test.json").getInputStream(), StandardCharsets.UTF_8);

        storageService
                .saveFile(new ClassPathResource("test.json").getInputStream(), "json")
                .flatMap(str -> Mono
                        .fromCallable(() -> StreamUtils
                                .copyToString(new FileInputStream("./target/upload/" + str), StandardCharsets.UTF_8)))
                .as(StepVerifier::create)
                .expectNext(text)
                .verifyComplete();

    }
}