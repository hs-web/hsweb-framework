package org.hsweb.web.service.resource;

import org.hsweb.web.bean.po.resource.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件服务接口，用于对服务器文件进行操作
 * Created by 浩 on 2015-11-26 0026.
 */
public interface FileService {
    /**
     * 保存一个文件流，并返回保存后生成的资源对象
     *
     * @param is       文件输入流
     * @param fileName 文件原始名称
     * @return 生成的资源对象
     * @throws Exception 保存失败的异常信息
     */
    Resources saveFile(InputStream is, String fileName) throws IOException;

    InputStream readResources(Resources resources) throws IOException;

    InputStream readResources(String resourceId) throws IOException;

    void writeResources(Resources resources, OutputStream outputStream) throws IOException;

    String getFileBasePath();


}
