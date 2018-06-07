package org.hswebframework.web.controller.file;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.entity.file.FileInfoEntity;
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
@Authorize(permission = "file-info", description = "文件信息管理")
@Api(value = "文件信息管理",tags = "文件管理-文件信息管理")
public class FileInfoController implements QueryController<FileInfoEntity, String, QueryParamEntity> {

    private FileInfoService fileInfoService;

    @Autowired
    public void setFileInfoService(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }

    @Override
    @SuppressWarnings("all")
    public FileInfoService getService() {
        return fileInfoService;
    }

}
