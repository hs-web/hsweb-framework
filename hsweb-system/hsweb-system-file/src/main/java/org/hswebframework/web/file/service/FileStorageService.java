package org.hswebframework.web.file.service;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.io.InputStream;

/**
 * 文件存储服务,用于保存文件信息到服务器
 *
 * @author zhouhao
 * @since 4.0.9
 */
public interface FileStorageService {

    /**
     * 保存文件,通常用来文件上传接口
     *
     * @param filePart FilePart
     * @return 文件访问地址
     */
    Mono<String> saveFile(FilePart filePart);

    /**
     * 使用文件流保存文件,并返回文件地址
     *
     * @param inputStream 文件输入流
     * @param fileType    文件类型,如: png,jpg
     * @return 文件访问地址
     */
    Mono<String> saveFile(InputStream inputStream, String fileType);
}
