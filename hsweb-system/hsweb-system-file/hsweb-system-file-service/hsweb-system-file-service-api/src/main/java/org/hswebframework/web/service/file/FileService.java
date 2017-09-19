package org.hswebframework.web.service.file;

import org.hswebframework.web.entity.file.FileInfoEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件服务,用户保存,读取文件信息
 *
 * @author zhouhao
 * @since 3.0
 */
public interface FileService {

    /**
     * 根据文件id或者md5获取文件流,如果文件不存在,将抛出{@link org.hswebframework.web.NotFoundException}
     *
     * @param fileIdOrMd5 文件id或者md5值
     * @return 文件流
     */
    InputStream readFile(String fileIdOrMd5);

    /**
     * 保存文件,并返回文件信息,如果存在相同的文件,则不会保存,而是返回已保存的文件
     *
     * @param fileStream 文件输入流
     * @param fileName   文件名称
     * @param type       文件类型(contentType)
     * @param creatorId  上传人
     * @return 文件信息
     * @throws IOException
     * @see FileInfoEntity
     */
    FileInfoEntity saveFile(InputStream fileStream, String fileName, String type, String creatorId) throws IOException;

    /**
     * 上传静态文件,并返回静态文件访问地址
     *
     * @param fileStream 文件输入流
     * @param fileName   文件名
     * @return 上传文件的访问地址
     * @throws IOException
     */
    String saveStaticFile(InputStream fileStream, String fileName) throws IOException;

    /**
     * 将已上传的文件写出到指定的输出流
     *
     * @param fileId 已上传的文件id
     * @param out    要写出的流
     * @param skip   跳过写出 {@link InputStream#skip(long)}
     * @throws IOException
     */
    void writeFile(String fileId, OutputStream out, long skip) throws IOException;

}
