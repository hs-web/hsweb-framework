package org.hswebframework.web.datasource;

import org.aopalliance.intercept.MethodInterceptor;
import org.hswebframework.web.ExpressionUtils;
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.hswebframework.web.datasource.exception.DataSourceNotFoundException;
import org.hswebframework.web.datasource.strategy.AnnotationDataSourceSwitchStrategyMatcher;
import org.hswebframework.web.datasource.strategy.DataSourceSwitchStrategyMatcher;
import org.hswebframework.web.datasource.strategy.ExpressionDataSourceSwitchStrategyMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hswebframework.web.datasource.strategy.AnnotationDataSourceSwitchStrategyMatcher.*;

/**
 * 通过aop方式进行对注解方式切换数据源提供支持
 *
 * @author zhouhao
 * @since 3.0
 */
@Configuration
public class AopDataSourceSwitcherAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "hsweb.datasource")
    public ExpressionDataSourceSwitchStrategyMatcher expressionDataSourceSwitchStrategyMatcher() {
        return new ExpressionDataSourceSwitchStrategyMatcher();
    }

    @Bean
    public AnnotationDataSourceSwitchStrategyMatcher annotationDataSourceSwitchStrategyMatcher() {
        return new AnnotationDataSourceSwitchStrategyMatcher();
    }

    @Bean
    public SwitcherMethodMatcherPointcutAdvisor switcherMethodMatcherPointcutAdvisor(List<DataSourceSwitchStrategyMatcher> matchers) {
        return new SwitcherMethodMatcherPointcutAdvisor(matchers);
    }

    public static class SwitcherMethodMatcherPointcutAdvisor extends StaticMethodMatcherPointcutAdvisor {
        private static final Logger logger           = LoggerFactory.getLogger(SwitcherMethodMatcherPointcutAdvisor.class);
        private static final long   serialVersionUID = 536295121851990398L;

        private List<DataSourceSwitchStrategyMatcher> matchers;

        private Map<AnnotationDataSourceSwitchStrategyMatcher.CacheKey, DataSourceSwitchStrategyMatcher> cache = new ConcurrentHashMap<>();

        public SwitcherMethodMatcherPointcutAdvisor(List<DataSourceSwitchStrategyMatcher> matchers) {
            this.matchers = matchers;
            setAdvice((MethodInterceptor) methodInvocation -> {
                CacheKey key = new CacheKey(ClassUtils.getUserClass(methodInvocation.getThis()), methodInvocation.getMethod());
                DataSourceSwitchStrategyMatcher matcher = cache.get(key);
                if (matcher == null) {
                    logger.warn("method:{} not support switch datasource", methodInvocation.getMethod());
                } else {
                    MethodInterceptorHolder holder = MethodInterceptorHolder.create(methodInvocation);
                    Strategy strategy = matcher.getStrategy(holder.createParamContext());
                    if (strategy == null) {
                        logger.warn("strategy matcher found:{}, but strategy is null!", matcher);
                    } else {
                        logger.debug("switch datasource.use strategy:{}", strategy);
                        if (strategy.isUseDefaultDataSource()) {
                            DataSourceHolder.switcher().useDefault();
                        } else {
                            String id = strategy.getDataSourceId();
                            if (id.contains("${")) {
                                id = ExpressionUtils.analytical(id, holder.getArgs(), "spel");
                            }
                            if (!DataSourceHolder.existing(id)) {
                                if (strategy.isFallbackDefault()) {
                                    DataSourceHolder.switcher().useDefault();
                                } else {
                                    throw new DataSourceNotFoundException(id);
                                }
                            } else {
                                DataSourceHolder.switcher().use(id);
                            }
                        }
                    }
                }
                try {
                    return methodInvocation.proceed();
                } finally {
                    DataSourceHolder.switcher().useLast();
                }
            });
        }

        @Override
        public boolean matches(Method method, Class<?> aClass) {
            CacheKey key = new CacheKey(aClass, method);
            matchers.stream()
                    .filter(matcher -> matcher.match(aClass, method))
                    .findFirst()
                    .ifPresent((matcher) -> cache.put(key, matcher));
            return cache.containsKey(key);
        }
    }
}
