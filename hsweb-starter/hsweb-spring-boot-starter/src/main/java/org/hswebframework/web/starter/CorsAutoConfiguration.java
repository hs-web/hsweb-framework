package org.hswebframework.web.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域设置
 *
 * @author zhouhao
 * @since 1.0
 */
@Configuration
@ConditionalOnProperty(prefix = "hsweb.cors", name = "enable", havingValue = "true")
public class CorsAutoConfiguration {

    @ConfigurationProperties(prefix = "hsweb.cors")
    @Bean
    public CorsConfiguration corsConfiguration() {
        return new CorsConfiguration();
    }

    @Bean
    public CorsFilter corsFilter(CorsConfiguration corsConfiguration) {
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(corsConfigurationSource);
    }
}
