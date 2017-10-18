package org.hswebframework.web.file.starter;

import org.hswebframework.web.service.file.fastdfs.FdfsFileService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since
 */
@Configuration
@ConditionalOnProperty(prefix = "hsweb.web.upload.fdfs", name = "enable", havingValue = "true")
public class FastdfsServiceAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "hsweb.web.upload")
    public FdfsFileService fdfsFileService() {
        return new FdfsFileService();
    }
}
