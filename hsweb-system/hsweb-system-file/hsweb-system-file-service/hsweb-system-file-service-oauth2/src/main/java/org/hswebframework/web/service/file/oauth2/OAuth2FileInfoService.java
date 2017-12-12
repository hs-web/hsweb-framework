package org.hswebframework.web.service.file.oauth2;

import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.entity.file.FileInfoEntity;
import org.hswebframework.web.service.file.FileInfoService;
import org.hswebframework.web.service.oauth2.AbstractOAuth2CrudService;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhouhao
 * @since
 */

public class OAuth2FileInfoService extends AbstractOAuth2CrudService<FileInfoEntity, String> implements FileInfoService {

    private String serviceId = "file-server";

    private String uriPrefix = "file-info";

    @Override
    public FileInfoEntity selectByMd5(String md5) {
        return selectSingle(QueryParamEntity
                .single(FileInfoEntity.md5, md5));
    }

    @Override
    public FileInfoEntity selectByIdOrMd5(String idOrMd5) {
        return selectSingle(QueryParamEntity
                .single(FileInfoEntity.id, idOrMd5)
                .or(FileInfoEntity.md5, idOrMd5));
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getUriPrefix() {
        return uriPrefix;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setUriPrefix(String uriPrefix) {
        this.uriPrefix = uriPrefix;
    }
}
