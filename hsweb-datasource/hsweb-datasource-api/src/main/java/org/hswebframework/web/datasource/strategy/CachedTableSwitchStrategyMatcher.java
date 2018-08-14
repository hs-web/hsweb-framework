package org.hswebframework.web.datasource.strategy;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Slf4j
public abstract class CachedTableSwitchStrategyMatcher implements TableSwitchStrategyMatcher {
    static Map<CacheKey, Strategy> cache = new ConcurrentHashMap<>();

    public abstract Strategy createStrategyIfMatch(Class target, Method method);

    @Override
    public boolean match(Class target, Method method) {
        Strategy strategy = createStrategyIfMatch(target, method);
        if (null != strategy) {
            if (log.isDebugEnabled()) {
                log.debug("create table switcher strategy:{} for method:{}", strategy, method);
            }
            CacheKey cacheKey = new CacheKey(target, method);
            cache.put(cacheKey, strategy);
            return true;
        }
        return false;
    }

    @Override
    public Strategy getStrategy(MethodInterceptorContext context) {
        Method method = context.getMethod();
        Class target = ClassUtils.getUserClass(context.getTarget());
        return cache.get(new CacheKey(target, method));
    }

    @AllArgsConstructor
    public static class CacheKey {

        private Class target;

        private Method method;

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CacheKey)) {
                return false;
            }
            CacheKey target = ((CacheKey) obj);
            return target.target == this.target && target.method == method;
        }

        public int hashCode() {
            int result = this.target != null ? this.target.hashCode() : 0;
            result = 31 * result + (this.method != null ? this.method.hashCode() : 0);
            return result;
        }
    }
}
