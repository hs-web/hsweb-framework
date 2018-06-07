package org.hswebframework.web.service.file.oauth2;

import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0
 */
@Configuration
@ConditionalOnBean(OAuth2RequestService.class)
public class OAuth2FileAutoConfiguration {

    @ConfigurationProperties(prefix = "hsweb.oauth2.file-server")
    @Bean
    public OAuth2FileInfoService oAuth2FileInfoService() {
        return new OAuth2FileInfoService();
    }

    @ConfigurationProperties(prefix = "hsweb.oauth2.file-server")
    @Bean
    public OAuth2FileService oAuth2FileService() {
        return new OAuth2FileService();
    }
}
