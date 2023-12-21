package org.hswebframework.web.system.authorization.defaults.configuration;

import org.hswebframework.web.system.authorization.defaults.webflux.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
public class AuthorizationWebAutoConfiguration {


    @AutoConfiguration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public static class WebFluxAuthorizationConfiguration {

        @Bean
        public WebFluxPermissionController webFluxPermissionController() {
            return new WebFluxPermissionController();
        }

        @Bean
        public WebFluxAuthorizationSettingController webFluxAuthorizationSettingController() {
            return new WebFluxAuthorizationSettingController();
        }

        @Bean
        @ConditionalOnProperty(prefix = "hsweb.authorization.dynamic-dimension", name = "enabled", havingValue = "true", matchIfMissing = true)
        public WebFluxDimensionController webFluxDimensionController() {
            return new WebFluxDimensionController();
        }


        @Bean
        @ConditionalOnMissingBean
        public WebFluxUserController webFluxUserController() {
            return new WebFluxUserController();
        }

        @Bean
        public WebFluxDimensionUserController webFluxDimensionUserController() {
            return new WebFluxDimensionUserController();
        }

        @Bean
        public WebFluxDimensionTypeController webFluxDimensionTypeController() {
            return new WebFluxDimensionTypeController();
        }
    }

}
