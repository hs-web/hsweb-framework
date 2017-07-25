package org.hswebframework.web.controller.file;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.entity.file.FileInfoEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.file.FileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件信息
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.file-info:file-info}")
@Authorize(permission = "file-info")
@AccessLogger("文件信息")
public class FileInfoController implements SimpleGenericEntityController<FileInfoEntity, String, QueryParamEntity> {

    private FileInfoService fileInfoService;

    @Autowired
    public void setFileInfoService(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }

    @Override
    public FileInfoService getService() {
        return fileInfoService;
    }
}
