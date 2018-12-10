package org.hswebframework.web.concurent;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.ExpressionUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.concurrent.RateLimiterManager;
import org.hswebframework.web.concurrent.annotation.RateLimiter;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * @author zhouhao
 * @since 3.0.4
 */
@Slf4j
public class RateLimiterAopAdvisor extends StaticMethodMatcherPointcutAdvisor {

    private static final long                    serialVersionUID = -1076122956392948260L;
    private static final ParameterNameDiscoverer nameDiscoverer   = new LocalVariableTableParameterNameDiscoverer();

    public RateLimiterAopAdvisor(RateLimiterManager rateLimiterManager) {
        setAdvice((MethodBeforeAdvice) (method, args, target) -> {
            String[] names = nameDiscoverer.getParameterNames(method);
            RateLimiter limiter = Optional.ofNullable(AnnotationUtils.findAnnotation(method, RateLimiter.class))
                    .orElseGet(() -> AnnotationUtils.findAnnotation(ClassUtils.getUserClass(target), RateLimiter.class));
            if (limiter != null) {
                List<String> keyExpressionList = new ArrayList<>(Arrays.asList(limiter.key()));
                if (keyExpressionList.isEmpty()) {
                    keyExpressionList.add(method.toString());
                }
                for (String keyExpress : keyExpressionList) {
                    if (keyExpress.contains("${")) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("user", Authentication.current().map(Authentication::getUser).orElse(null));
                        for (int i = 0; i < args.length; i++) {
                            params.put(names.length > i ? names[i] : "arg" + i, args[i]);
                            params.put("arg" + i, args[i]);

                        }
                        keyExpress = ExpressionUtils.analytical(keyExpress, params, "spel");
                    }
                    log.debug("do rate limiter:[{}]. ", keyExpress);
                    boolean success = rateLimiterManager
                            .getRateLimiter(keyExpress, limiter.permits(), limiter.timeUnit())
                            .tryAcquire(limiter.acquire(), limiter.acquireTimeUnit());
                    if (!success) {
                        throw new TimeoutException("请求超时");
                    }
                }
            }
        });
    }


    @Override
    public boolean matches(Method method, Class<?> targetClass) {

        return AnnotationUtils.findAnnotation(method, RateLimiter.class) != null
                || AnnotationUtils.findAnnotation(targetClass, RateLimiter.class) != null;
    }
}
