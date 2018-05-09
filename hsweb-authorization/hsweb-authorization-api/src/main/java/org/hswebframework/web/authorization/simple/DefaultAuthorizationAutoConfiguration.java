package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationHolder;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.builder.AuthenticationBuilderFactory;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.simple.builder.DataAccessConfigConvert;
import org.hswebframework.web.authorization.simple.builder.SimpleAuthenticationBuilderFactory;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.token.DefaultUserTokenManager;
import org.hswebframework.web.authorization.token.UserTokenAuthenticationSupplier;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.convert.CustomMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author zhouhao
 */
@Configuration
public class DefaultAuthorizationAutoConfiguration {

    @Autowired(required = false)
    private List<DataAccessConfigConvert> dataAccessConfigConverts;

    @Bean
    @ConditionalOnMissingBean(UserTokenManager.class)
    @ConfigurationProperties(prefix = "hsweb.authorize")
    public UserTokenManager userTokenManager() {
        return new DefaultUserTokenManager();
    }

    @Bean
    @ConditionalOnBean(AuthenticationManager.class)
    public UserTokenAuthenticationSupplier userTokenAuthenticationSupplier(AuthenticationManager authenticationManager) {
        UserTokenAuthenticationSupplier supplier = new UserTokenAuthenticationSupplier(authenticationManager);
        AuthenticationHolder.addSupplier(supplier);
        return supplier;
    }

    @Bean
    @ConditionalOnMissingBean(DataAccessConfigBuilderFactory.class)
    @ConfigurationProperties(prefix = "hsweb.authorization.data-access", ignoreInvalidFields = true)
    public SimpleDataAccessConfigBuilderFactory dataAccessConfigBuilderFactory() {
        SimpleDataAccessConfigBuilderFactory factory = new SimpleDataAccessConfigBuilderFactory();
        if (null != dataAccessConfigConverts) {
            dataAccessConfigConverts.forEach(factory::addConvert);
        }
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationBuilderFactory.class)
    public AuthenticationBuilderFactory authenticationBuilderFactory(DataAccessConfigBuilderFactory dataAccessConfigBuilderFactory) {
        return new SimpleAuthenticationBuilderFactory(dataAccessConfigBuilderFactory);
    }

    @Bean
    public CustomMessageConverter authenticationCustomMessageConverter(AuthenticationBuilderFactory factory) {
        return new CustomMessageConverter() {
            @Override
            public boolean support(Class clazz) {
                return clazz == Authentication.class;
            }

            @Override
            public Object convert(Class clazz, byte[] message) {
                String json = new String(message);

                return factory.create().json(json).build();
            }
        };
    }
}
