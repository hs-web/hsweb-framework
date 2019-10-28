package org.hswebframework.web.file.web;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.ResourceAction;
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
public class ReactiveFileController {

    @Autowired
    private FileUploadProperties properties;

    @ResourceAction(id = "upload-static", name = "静态文件")
    @PostMapping("/static")
    @SneakyThrows
    public Mono<String> uploadStatic(@RequestPart("file") Part part) {
        FileUploadProperties.StaticFileInfo name ;
        if(part instanceof FilePart){
            name = properties.createStaticSavePath(((FilePart)part).filename());
            return ((FilePart)part)
                    .transferTo(new File(name.getSavePath()))
                    .thenReturn(name.getLocation());
        }else{
           return Mono.error(()->new IllegalArgumentException("[file] part is not a file"));
        }

    }

}
