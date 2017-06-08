package org.hswebframework.web.concurrent.lock.starter;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hswebframework.web.AopUtils;
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.hswebframework.web.concurrent.lock.LockManager;
import org.hswebframework.web.concurrent.lock.annotation.Lock;
import org.hswebframework.web.concurrent.lock.annotation.ReadLock;
import org.hswebframework.web.concurrent.lock.annotation.WriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class AopLockAdvisor extends StaticMethodMatcherPointcutAdvisor {

    public AopLockAdvisor(LockManager lockManager) {
        Objects.requireNonNull(lockManager);
        setAdvice((MethodInterceptor) methodInvocation -> {
            MethodInterceptorHolder holder = MethodInterceptorHolder.create(methodInvocation);
            Lock lockAnn = holder.findMethodAnnotation(Lock.class);
            ReadLock readLockAnn = holder.findMethodAnnotation(ReadLock.class);
            WriteLock writeLock = holder.findMethodAnnotation(WriteLock.class);
            List<LockProcessor> lockProcessors = new ArrayList<>();
            if (null != lockAnn) {
                lockProcessors.add(initLockInfo(lockAnn.timeout(), lockAnn.timeUnit(),
                        LockProcessor.<Lock, java.util.concurrent.locks.Lock>build(lockAnn, holder)
                                .lockNameIs(Lock::value)
                                .lockIs(lockManager::getLock)));
            }
            if (null != readLockAnn) {
                lockProcessors.add(initLockInfo(readLockAnn.timeout(), readLockAnn.timeUnit(),
                        LockProcessor.<ReadLock, java.util.concurrent.locks.Lock>build(readLockAnn, holder)
                                .lockNameIs(ReadLock::value)
                                .lockIs(name -> lockManager.getReadWriteLock(name).readLock())));
            }
            if (null != writeLock) {
                lockProcessors.add(initLockInfo(writeLock.timeout(), writeLock.timeUnit(),
                        LockProcessor.<WriteLock, java.util.concurrent.locks.Lock>build(writeLock, holder)
                                .lockNameIs(WriteLock::value)
                                .lockIs(name -> lockManager.getReadWriteLock(name).writeLock())));
            }

            try {
                for (LockProcessor processor : lockProcessors) {
                    Throwable e = processor.doLock();
                    if (e != null) {
                        throw e;
                    }
                }
                return methodInvocation.proceed();
            } finally {
                for (LockProcessor processor : lockProcessors) {
                    processor.doUnlock();
                }
            }
        });
    }

    protected <A extends Annotation> LockProcessor<A, java.util.concurrent.locks.Lock> initLockInfo(long timeout, TimeUnit timeUnit, LockProcessor<A, java.util.concurrent.locks.Lock> lockProcessor) {
        return lockProcessor.lock(lock -> {
            try {
                lock.tryLock(timeout, timeUnit);
                return null;
            } catch (InterruptedException e) {
                return e;
            }
        }).unlock(lock -> {
            try {
                lock.unlock();
                return null;
            } catch (Throwable e) {
                return e;
            }
        }).init();
    }


    @Override
    public boolean matches(Method method, Class<?> aClass) {
        Lock lock = AopUtils.findMethodAnnotation(aClass, method, Lock.class);
        if (null != lock) return true;
        ReadLock readLock = AopUtils.findMethodAnnotation(aClass, method, ReadLock.class);
        if (null != readLock) return true;
        WriteLock writeLock = AopUtils.findMethodAnnotation(aClass, method, WriteLock.class);
        if (null != writeLock) return true;
        return false;
    }
}
