package org.hswebframework.web.authorization.basic.configuration;

import org.hswebframework.web.authorization.basic.aop.AopAuthorizingController;
import org.hswebframework.web.authorization.basic.aop.AopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.basic.aop.DefaultAopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.basic.handler.AuthorizingHandler;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 */
@Configuration
@AutoConfigureAfter(AuthorizingHandlerAutoConfiguration.class)
public class AopAuthorizeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AopMethodAuthorizeDefinitionParser.class)
    public DefaultAopMethodAuthorizeDefinitionParser defaultAopMethodAuthorizeDefinitionParser() {
        return new DefaultAopMethodAuthorizeDefinitionParser();
    }


    @Bean
    public AopAuthorizingController aopAuthorizingController(AuthorizingHandler authorizingHandler,
                                                             AopMethodAuthorizeDefinitionParser aopMethodAuthorizeDefinitionParser) {
        return new AopAuthorizingController(authorizingHandler, aopMethodAuthorizeDefinitionParser);
    }
}
