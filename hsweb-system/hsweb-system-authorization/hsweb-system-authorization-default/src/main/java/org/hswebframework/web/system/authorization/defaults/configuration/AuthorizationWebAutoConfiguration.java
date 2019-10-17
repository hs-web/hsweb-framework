package org.hswebframework.web.system.authorization.defaults.configuration;

import org.hswebframework.web.system.authorization.defaults.webflux.WebFluxPermissionController;
import org.hswebframework.web.system.authorization.defaults.webflux.WebFluxUserController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthorizationWebAutoConfiguration {


    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public static class WebFluxAuthorizationConfiguration {

        @Bean
        public WebFluxPermissionController webFluxPermissionController() {
            return new WebFluxPermissionController();
        }

        @Bean
        public WebFluxUserController webFluxUserController() {
            return new WebFluxUserController();
        }
    }

}
