package org.hswebframework.web.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hsweb.file.upload.s3")
@Data
public class S3FileProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region;
    private String baseUrl;
}