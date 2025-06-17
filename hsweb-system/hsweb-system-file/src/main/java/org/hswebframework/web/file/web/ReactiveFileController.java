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
import org.hswebframework.web.file.service.FileStorageService;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@Resource(id = "file", name = "文件上传")
@Slf4j
@RequestMapping("/file")
@Tag(name = "文件上传")
public class ReactiveFileController {

    private final FileUploadProperties properties;

    private final FileStorageService fileStorageService;


    public ReactiveFileController(FileUploadProperties properties, FileStorageService fileStorageService) {
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
                        return fileStorageService.saveFile(filePart);
                    } else {
                        return Mono.error(() -> new IllegalArgumentException("[file] part is not a file"));
                    }
                });

    }

    @PostMapping(value = "/static/stream", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "上传文件流")
    public Mono<String> uploadOssStream(ServerHttpRequest request,
                                     @RequestParam("fileType") String fileType) {

        if (properties.denied("upload." + fileType, MediaType.APPLICATION_OCTET_STREAM)) {
            return Mono.error(new AccessDenyException());
        }

        return DataBufferUtils.join(request.getBody())
                .flatMap(dataBuffer -> {
                    InputStream inputStream = dataBuffer.asInputStream(true);
                    return fileStorageService.saveFile(inputStream, fileType);
                });
    }

}
