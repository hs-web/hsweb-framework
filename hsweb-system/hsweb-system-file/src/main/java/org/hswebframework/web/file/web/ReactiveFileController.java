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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.File;

@RestController
@Resource(id = "file", name = "文件上传")
@Slf4j
@RequestMapping("/file")
@Tag(name = "文件上传")
public class ReactiveFileController {

    @Autowired
    private FileUploadProperties properties;

    @PostMapping("/static")
    @SneakyThrows
    @ResourceAction(id = "upload-static", name = "静态文件")
    @Operation(summary = "上传静态文件")
    public Mono<String> uploadStatic(@RequestPart("file")
                                     @Parameter(name = "file", description = "文件", style = ParameterStyle.FORM) Part part) {
        FileUploadProperties.StaticFileInfo name;
        if (part instanceof FilePart) {
            FilePart filePart = ((FilePart) part);
            if (properties.denied(filePart.filename(), filePart.headers().getContentType())) {
                throw new AccessDenyException();
            }
            name = properties.createStaticSavePath(filePart.filename());
            return ((FilePart) part)
                    .transferTo(new File(name.getSavePath()))
                    .thenReturn(name.getLocation());
        } else {
            return Mono.error(() -> new IllegalArgumentException("[file] part is not a file"));
        }

    }

}
