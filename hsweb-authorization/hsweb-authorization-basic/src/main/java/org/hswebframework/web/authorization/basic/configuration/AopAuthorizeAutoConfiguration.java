package org.hswebframework.web.authorization.basic.configuration;

import org.hswebframework.web.authorization.basic.aop.AopAuthorizingController;
import org.hswebframework.web.authorization.basic.aop.AopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.basic.aop.DefaultAopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.basic.handler.AuthorizingHandler;
import org.hswebframework.web.i18n.MessageSourceInitializer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 */
@Configuration
@AutoConfigureAfter(AuthorizingHandlerAutoConfiguration.class)
public class AopAuthorizeAutoConfiguration {

    @Bean
    @ConditionalOnClass(MessageSource.class)
    @ConditionalOnMissingBean(AopMethodAuthorizeDefinitionParser.class)
    public DefaultAopMethodAuthorizeDefinitionParser defaultAopMethodAuthorizeDefinitionParser() {
        return new DefaultAopMethodAuthorizeDefinitionParser();
    }


    @Bean
    @ConfigurationProperties(prefix = "hsweb.authorize")
    public AopAuthorizingController aopAuthorizingController(AuthorizingHandler authorizingHandler,
                                                             AopMethodAuthorizeDefinitionParser aopMethodAuthorizeDefinitionParser) {

        return  new AopAuthorizingController(authorizingHandler, aopMethodAuthorizeDefinitionParser);
    }

}
