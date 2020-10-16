package org.hswebframework.web.oauth2.configuration;

import org.hswebframework.web.oauth2.server.OAuth2ClientManager;
import org.hswebframework.web.oauth2.service.InDBOAuth2ClientManager;
import org.hswebframework.web.oauth2.service.OAuth2ClientService;
import org.hswebframework.web.oauth2.web.WebFluxOAuth2ClientController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OAuth2ClientManagerAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveOAuth2ClientManagerAutoConfiguration {

        @Bean
        public OAuth2ClientService oAuth2ClientService() {
            return new OAuth2ClientService();
        }

        @Bean
        @ConditionalOnMissingBean
        public OAuth2ClientManager oAuth2ClientManager(OAuth2ClientService clientService) {
            return new InDBOAuth2ClientManager(clientService);
        }

        @Bean
        @ConditionalOnMissingBean
        public WebFluxOAuth2ClientController webFluxOAuth2ClientController(OAuth2ClientService clientService){
            return new WebFluxOAuth2ClientController(clientService);
        }
    }

}
