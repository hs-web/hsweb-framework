package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.*;
import org.hswebframework.web.authorization.builder.AuthenticationBuilderFactory;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.dimension.DimensionManager;
import org.hswebframework.web.authorization.dimension.DimensionUserBindProvider;
import org.hswebframework.web.authorization.simple.builder.SimpleAuthenticationBuilderFactory;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.hswebframework.web.authorization.token.*;
import org.hswebframework.web.authorization.twofactor.TwoFactorValidatorManager;
import org.hswebframework.web.authorization.twofactor.defaults.DefaultTwoFactorValidatorManager;
import org.hswebframework.web.convert.CustomMessageConverter;
import org.jetlinks.json.Json;
import org.jetlinks.json.JsonMapper;
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

    // 延迟取得所属上下文的门面；配置类创建期间不触发原生 mapper 或应用全局初始化。
    @Autowired
    private ObjectProvider<JsonMapper> jsonMappers;

    private JsonMapper jsonMapper() {
        // 旧工厂方法也允许脱离容器调用，此时使用 facade 默认入口。
        return jsonMappers == null ? Json.global() : jsonMappers.getIfAvailable(Json::global);
    }

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
        return new SimpleDataAccessConfigBuilderFactory(jsonMapper());
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationBuilderFactory.class)
    public AuthenticationBuilderFactory authenticationBuilderFactory(DataAccessConfigBuilderFactory dataAccessConfigBuilderFactory) {
        return new SimpleAuthenticationBuilderFactory(dataAccessConfigBuilderFactory,
            jsonMapper());
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
