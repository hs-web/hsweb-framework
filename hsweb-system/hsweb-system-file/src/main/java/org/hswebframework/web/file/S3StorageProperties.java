package org.hswebframework.web.file;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Locale;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "oss.s3")
public class S3StorageProperties {
    private String endpoint;
    private String region;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String baseUrl;

    private Set<String> allowFiles;

    private Set<String> denyFiles;

    private Set<String> allowMediaType;

    private Set<String> denyMediaType;

    private Set<PosixFilePermission> permissions;


    public void applyFilePermission(File file) {

        if (CollectionUtils.isEmpty(permissions)) {
            return;
        }
        try {
            Path path = Paths.get(file.toURI());
            PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
            view.setPermissions(permissions);
        } catch (Throwable ignore) {
            // 失败时忽略，兼容Windows等不支持Posix的系统
        }
    }

    public boolean denied(String name, MediaType mediaType) {
        String suffix = (name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : "").toLowerCase(Locale.ROOT);
        boolean defaultDeny = false;
        if (CollectionUtils.isNotEmpty(denyFiles)) {
            if (denyFiles.contains(suffix)) {
                return true;
            }
            defaultDeny = false;
        }

        if (CollectionUtils.isNotEmpty(allowFiles)) {
            if (allowFiles.contains(suffix)) {
                return false;
            }
            defaultDeny = true;
        }

        if (CollectionUtils.isNotEmpty(denyMediaType)) {
            if (denyMediaType.contains(mediaType.toString())) {
                return true;
            }
            defaultDeny = false;
        }

        if (CollectionUtils.isNotEmpty(allowMediaType)) {
            if (allowMediaType.contains(mediaType.toString())) {
                return false;
            }
            defaultDeny = true;
        }

        return defaultDeny;
    }
}
