package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.*;
import org.hswebframework.web.authorization.builder.AuthenticationBuilderFactory;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.dimension.DimensionManager;
import org.hswebframework.web.authorization.dimension.DimensionUserBindProvider;
import org.hswebframework.web.authorization.simple.builder.DataAccessConfigConverter;
import org.hswebframework.web.authorization.simple.builder.SimpleAuthenticationBuilderFactory;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.token.*;
import org.hswebframework.web.authorization.twofactor.TwoFactorValidatorManager;
import org.hswebframework.web.authorization.twofactor.defaults.DefaultTwoFactorValidatorManager;
import org.hswebframework.web.convert.CustomMessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author zhouhao
 */
@AutoConfiguration
public class DefaultAuthorizationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(UserTokenManager.class)
    @ConfigurationProperties(prefix = "hsweb.user-token")
    public UserTokenManager userTokenManager() {
        return new DefaultUserTokenManager();
    }

    @Bean
    @ConditionalOnMissingBean
//    @ConditionalOnBean(ReactiveAuthenticationManagerProvider.class)
    public ReactiveAuthenticationManager reactiveAuthenticationManager(List<ReactiveAuthenticationManagerProvider> providers) {
        return new CompositeReactiveAuthenticationManager(providers);
    }

    @Bean
    @ConditionalOnBean(ReactiveAuthenticationManager.class)
    public UserTokenReactiveAuthenticationSupplier userTokenReactiveAuthenticationSupplier(UserTokenManager userTokenManager,
                                                                                           ReactiveAuthenticationManager authenticationManager) {
        UserTokenReactiveAuthenticationSupplier supplier = new UserTokenReactiveAuthenticationSupplier(userTokenManager, authenticationManager);
        ReactiveAuthenticationHolder.addSupplier(supplier);
        return supplier;
    }

    @Bean
    @ConditionalOnBean(AuthenticationManager.class)
    public UserTokenAuthenticationSupplier userTokenAuthenticationSupplier(UserTokenManager userTokenManager,
                                                                           AuthenticationManager authenticationManager) {
        UserTokenAuthenticationSupplier supplier = new UserTokenAuthenticationSupplier(userTokenManager, authenticationManager);
        AuthenticationHolder.addSupplier(supplier);
        return supplier;
    }

    @Bean
    @ConditionalOnMissingBean(DataAccessConfigBuilderFactory.class)
    @ConfigurationProperties(prefix = "hsweb.authorization.data-access", ignoreInvalidFields = true)
    public SimpleDataAccessConfigBuilderFactory dataAccessConfigBuilderFactory() {
        return new SimpleDataAccessConfigBuilderFactory();
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

    @Bean
    @ConditionalOnMissingBean(DimensionManager.class)
    public DimensionManager defaultDimensionManager(ObjectProvider<DimensionUserBindProvider>bindProviders,
                                                    ObjectProvider<DimensionProvider> providers){
        DefaultDimensionManager manager =  new DefaultDimensionManager();
        bindProviders.forEach(manager::addBindProvider);
        providers.forEach(manager::addProvider);

        return manager;
    }
}
