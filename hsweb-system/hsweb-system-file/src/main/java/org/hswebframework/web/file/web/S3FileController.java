package org.hswebframework.web.file.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.ResourceAction;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.file.FileUploadProperties;
import org.hswebframework.web.file.S3StorageProperties;
import org.hswebframework.web.file.service.FileStorageService;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@Resource(id = "ossFile", name = "oss文件上传")
@Slf4j
@RequestMapping("/ossFile")
@Tag(name = "oss文件上传")
public class S3FileController {
    private final S3StorageProperties properties;

    private final FileStorageService fileStorageService;


    public S3FileController(S3StorageProperties properties, FileStorageService fileStorageService) {
        this.properties = properties;
        this.fileStorageService = fileStorageService;
    }


    @PostMapping("/static")
    @SneakyThrows
    @ResourceAction(id = "upload-static", name = "静态文件")
    @Operation(summary = "上传静态文件")
    public Mono<String> uploadStatic(@RequestPart("file")
                                     @Parameter(name = "file", description = "文件", style = ParameterStyle.FORM) Mono<Part> partMono) {
        return partMono
                .flatMap(part -> {
                    if (part instanceof FilePart) {
                        FilePart filePart = ((FilePart) part);
                        if (properties.denied(filePart.filename(), filePart.headers().getContentType())) {
                            return Mono.error( new AccessDenyException());
                        }
                        try {
                            return fileStorageService.saveFile(filePart);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        return Mono.error(() -> new IllegalArgumentException("[file] part is not a file"));
                    }
                });

    }
}
