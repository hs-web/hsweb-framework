package org.hswebframework.web.service.file.oauth2;

import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.entity.file.FileInfoEntity;
import org.hswebframework.web.entity.file.SimpleFileInfoEntity;
import org.hswebframework.web.service.file.FileService;
import org.hswebframework.web.service.oauth2.OAuth2ServiceSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author zhouhao
 * @since 3.0
 */
@ConfigurationProperties(prefix = "hsweb.oauth2.file-server")
public class OAuth2FileService implements FileService, OAuth2ServiceSupport {
    private String serviceId = "file-server";

    private String uriPrefix = "file";

    @Autowired
    private OAuth2RequestService requestService;

    @Override
    public InputStream readFile(String fileIdOrMd5) {
        return createRequest("/md5/download/" + fileIdOrMd5)
                .get()
                .as(OAuth2Response::asStream);
    }

    @Override
    public FileInfoEntity saveFile(InputStream fileStream, String fileName, String type, String creatorId) throws IOException {
        return createRequest("/upload")
                .upload("file", fileStream, fileName)
                .as(getEntityType());
    }

    @Override
    public String saveStaticFile(InputStream fileStream, String fileName) throws IOException {
        return createRequest("/upload-static")
                .upload("file", fileStream, fileName)
                .as(String.class);
    }

    @Override
    public void writeFile(String fileId, OutputStream out, long skip) throws IOException {
        try (InputStream stream = createRequest("/download/" + fileId)
                .header("Range", "bytes=" + skip)
                .get().asStream()) {
            StreamUtils.copy(stream, out);
        }
    }

    @Override
    public OAuth2RequestService getRequestService() {
        return requestService;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getUriPrefix() {
        return uriPrefix;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<SimpleFileInfoEntity> getEntityType() {
        return SimpleFileInfoEntity.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<String> getPrimaryKeyType() {
        return String.class;
    }
}
