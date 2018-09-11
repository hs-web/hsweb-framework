package org.hswebframework.web.datasource;

import org.aopalliance.intercept.MethodInterceptor;
import org.hswebframework.web.ExpressionUtils;
import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.hswebframework.web.datasource.exception.DataSourceNotFoundException;
import org.hswebframework.web.datasource.strategy.*;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

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
    public TableSwitchStrategyMatcher alwaysNoMatchStrategyMatcher() {
        return new TableSwitchStrategyMatcher() {
            @Override
            public boolean match(Class target, Method method) {
                return false;
            }

            @Override
            public Strategy getStrategy(MethodInterceptorContext context) {
                return null;
            }
        };
    }

    @Bean
    public SwitcherMethodMatcherPointcutAdvisor switcherMethodMatcherPointcutAdvisor(
            List<DataSourceSwitchStrategyMatcher> matchers,
            List<TableSwitchStrategyMatcher> tableSwitcher) {
        return new SwitcherMethodMatcherPointcutAdvisor(matchers, tableSwitcher);
    }

    public static class SwitcherMethodMatcherPointcutAdvisor extends StaticMethodMatcherPointcutAdvisor {
        private static final Logger logger           = LoggerFactory.getLogger(SwitcherMethodMatcherPointcutAdvisor.class);
        private static final long   serialVersionUID = 536295121851990398L;

        private List<DataSourceSwitchStrategyMatcher> matchers;

        private List<TableSwitchStrategyMatcher> tableSwitcher;

        private Map<CachedDataSourceSwitchStrategyMatcher.CacheKey, DataSourceSwitchStrategyMatcher> cache
                = new ConcurrentHashMap<>();
        private Map<CachedTableSwitchStrategyMatcher.CacheKey, TableSwitchStrategyMatcher>           tableCache
                = new ConcurrentHashMap<>();

        public SwitcherMethodMatcherPointcutAdvisor(List<DataSourceSwitchStrategyMatcher> matchers,
                                                    List<TableSwitchStrategyMatcher> tableSwitcher) {
            this.matchers = matchers;
            this.tableSwitcher = tableSwitcher;
            setAdvice((MethodInterceptor) methodInvocation -> {
                CacheKey key = new CacheKey(ClassUtils.getUserClass(methodInvocation.getThis()), methodInvocation.getMethod());
                CachedTableSwitchStrategyMatcher.CacheKey tableKey = new CachedTableSwitchStrategyMatcher.CacheKey(ClassUtils.getUserClass(methodInvocation.getThis()), methodInvocation.getMethod());

                DataSourceSwitchStrategyMatcher matcher = cache.get(key);
                TableSwitchStrategyMatcher tableMatcher = tableCache.get(tableKey);

                Consumer<MethodInterceptorContext> before = context -> {
                };
                AtomicBoolean dataSourceChanged = new AtomicBoolean(true);
                if (matcher != null) {
                    before = before.andThen(context -> {
                        Strategy strategy = matcher.getStrategy(context);
                        if (strategy == null) {
                            dataSourceChanged.set(false);
                            logger.warn("strategy matcher found:{}, but strategy is null!", matcher);
                        } else {
                            logger.debug("switch datasource.use strategy:{}", strategy);
                            if (strategy.isUseDefaultDataSource()) {
                                DataSourceHolder.switcher().useDefault();
                            } else {
                                try {
                                    String id = strategy.getDataSourceId();
                                    if (id.contains("${")) {
                                        id = ExpressionUtils.analytical(id, context.getParams(), "spel");
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
                                } catch (RuntimeException e) {
                                    dataSourceChanged.set(false);
                                    throw e;
                                } catch (Exception e) {
                                    dataSourceChanged.set(false);
                                    throw new RuntimeException(e.getMessage(), e);
                                }
                            }
                        }
                    });
                }
                if (tableMatcher != null) {
                    before = before.andThen(context -> {
                        TableSwitchStrategyMatcher.Strategy strategy = tableMatcher.getStrategy(context);
                        if (null != strategy) {
                            logger.debug("switch table. use strategy:{}", strategy);
                            strategy.getMapping().forEach(DataSourceHolder.tableSwitcher()::use);
                        } else {
                            logger.warn("table strategy matcher found:{}, but strategy is null!", matcher);
                        }
                    });
                }

                MethodInterceptorHolder holder = MethodInterceptorHolder.create(methodInvocation);
                before.accept(holder.createParamContext());
                try {
                    return methodInvocation.proceed();
                } finally {
                    if (dataSourceChanged.get()) {
                        DataSourceHolder.switcher().useLast();
                    }
                    DataSourceHolder.tableSwitcher().reset();
                }
            });
        }

        @Override
        public boolean matches(Method method, Class<?> aClass) {
            Class<?> targetClass = ClassUtils.getUserClass(aClass);

            CacheKey key = new CacheKey(targetClass, method);
            matchers.stream()
                    .filter(matcher -> matcher.match(targetClass, method))
                    .findFirst()
                    .ifPresent((matcher) -> cache.put(key, matcher));

            boolean datasourceMatched = cache.containsKey(key);
            boolean tableMatched = false;
            if (null != tableSwitcher) {
                CachedTableSwitchStrategyMatcher.CacheKey tableCacheKey = new CachedTableSwitchStrategyMatcher
                        .CacheKey(targetClass, method);
                tableSwitcher.stream()
                        .filter(matcher -> matcher.match(targetClass, method))
                        .findFirst()
                        .ifPresent((matcher) -> tableCache.put(tableCacheKey, matcher));
                tableMatched = tableCache.containsKey(tableCacheKey);
            }

            return datasourceMatched || tableMatched;
        }
    }
}
