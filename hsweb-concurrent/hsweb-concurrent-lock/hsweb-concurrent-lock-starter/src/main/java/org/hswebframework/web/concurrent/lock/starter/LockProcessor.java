package org.hswebframework.web.concurrent.lock.starter;


import org.hswebframework.web.AopUtils;
import org.hswebframework.web.ExpressionUtils;
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@SuppressWarnings("unchecked")
public class LockProcessor<A extends Annotation, L> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private A lockAnn;

    private MethodInterceptorHolder interceptorHolder;

    private Function<A, String[]> lockNameGetter;

    private Function<String, L> lockGetter;

    private Function<L, Throwable> lockAccepter;

    private Function<L, Throwable> unlockAccepter;

    private Map<String, L> lockStore = new HashMap<>();

    private LockProcessor() {
    }

    public static <A extends Annotation, L> LockProcessor<A, L> build(A annotation, MethodInterceptorHolder holder) {
        LockProcessor<A, L> alLockProcessor = new LockProcessor<>();
        alLockProcessor.lockAnn = annotation;
        alLockProcessor.interceptorHolder = holder;
        return alLockProcessor;
    }

    public LockProcessor<A, L> lockNameIs(Function<A, String[]> lockNameGetter) {
        this.lockNameGetter = lockNameGetter;
        return this;
    }

    public LockProcessor<A, L> lockIs(Function<String, L> lockGetter) {
        this.lockGetter = lockGetter;
        return this;
    }

    public LockProcessor<A, L> lock(Function<L, Throwable> lockAccepter) {
        this.lockAccepter = lockAccepter;
        return this;
    }

    public LockProcessor<A, L> unlock(Function<L, Throwable> unlockAccepter) {
        this.unlockAccepter = unlockAccepter;
        return this;
    }

    public LockProcessor<A, L> init() {
        Objects.requireNonNull(lockAnn);
        Objects.requireNonNull(interceptorHolder);
        Objects.requireNonNull(lockNameGetter);
        String[] lockNameArr = lockNameGetter.apply(lockAnn);
        if (lockNameArr.length == 0) {
            String name = createLockName(null);
            lockStore.put(name, lockGetter.apply(name));
        } else {
            for (String expression : lockNameArr) {
                String name = createLockName(expression);
                lockStore.put(name, lockGetter.apply(name));
            }
        }
        return this;
    }

    protected String createLockName(String expression) {
        try {
            if (StringUtils.isEmpty(expression)) {
                return interceptorHolder.getMethod().getName().concat("_").concat(interceptorHolder.getId());
            }
            return ExpressionUtils.analytical(expression, interceptorHolder.getArgs(), "spel");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<L> successLock = new ArrayList<>();

    public Throwable doLock() {
        Throwable lockError = null;
        for (Map.Entry<String, L> lock : lockStore.entrySet()) {
            Throwable error = lockAccepter.apply(lock.getValue());
            if (error == null) {
                successLock.add(lock.getValue());
            } else {
                lockError = error;
            }
        }
        return lockError;
    }

    public void doUnlock() {
        for (L lock : successLock) {
            Throwable error = unlockAccepter.apply(lock);
            if (null != error)
                logger.error("unlock {} error", interceptorHolder.getMethod(), error);
        }
    }

}
