package org.hswebframework.web.authorization.basic.configuration;

import org.hswebframework.web.authorization.basic.aop.AopAuthorizingController;
import org.hswebframework.web.authorization.basic.aop.AopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.basic.aop.DefaultAopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.basic.handler.AuthorizingHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * @author zhouhao
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(AuthorizingHandlerAutoConfiguration.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AopAuthorizeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AopMethodAuthorizeDefinitionParser.class)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public DefaultAopMethodAuthorizeDefinitionParser defaultAopMethodAuthorizeDefinitionParser() {
        return new DefaultAopMethodAuthorizeDefinitionParser();
    }


    @Bean
    @ConfigurationProperties(prefix = "hsweb.authorize")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public AopAuthorizingController aopAuthorizingController(AuthorizingHandler authorizingHandler,
                                                             AopMethodAuthorizeDefinitionParser aopMethodAuthorizeDefinitionParser) {

        return  new AopAuthorizingController(authorizingHandler, aopMethodAuthorizeDefinitionParser);
    }

}
