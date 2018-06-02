package org.hswebframework.web.service.file;

import org.hswebframework.web.entity.file.FileInfoEntity;
import org.hswebframework.web.service.CrudService;

/**
 * 文件信息 服务类
 *
 * @author hsweb-generator-online
 */
public interface FileInfoService extends CrudService<FileInfoEntity, String> {
    FileInfoEntity selectByMd5(String md5);

    FileInfoEntity selectByIdOrMd5(String idOrMd5);
}
