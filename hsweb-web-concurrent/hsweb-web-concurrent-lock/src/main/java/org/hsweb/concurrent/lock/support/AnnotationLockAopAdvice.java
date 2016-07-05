package org.hsweb.concurrent.lock.support;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hsweb.concurrent.lock.LockFactory;
import org.hsweb.concurrent.lock.annotation.LockName;
import org.hsweb.concurrent.lock.exception.LockException;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;
import org.hsweb.expands.script.engine.ExecuteResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Created by zhouhao on 16-5-13.
 */
@Aspect
public class AnnotationLockAopAdvice {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LockFactory lockFactory;
    private ConcurrentMap<String, Lock> lockMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String, ReadWriteLock> readWriteLockMap = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private Map<String, ExpressionScopeBean> expressionScopeBeanMap;

    @Around("@annotation(unLock)")
    public Object unWriteLock(ProceedingJoinPoint pjp,
                              org.hsweb.concurrent.lock.annotation.UnWriteLock unLock) throws Throwable {
        String name = getLockName(pjp);
        ReadWriteLock lock = readWriteLockMap.get(name);
        try {
            return pjp.proceed();
        } finally {
            logger.debug("unlock :{}", name);
            if (lock != null)
                unlock(lock.writeLock());
        }
    }

    @Around("@annotation(unLock)")
    public Object unReadLock(ProceedingJoinPoint pjp,
                             org.hsweb.concurrent.lock.annotation.UnReadLock unLock) throws Throwable {
        String name = getLockName(pjp);
        ReadWriteLock lock = readWriteLockMap.get(name);
        try {
            return pjp.proceed();
        } finally {
            logger.debug("unlock :{}", name);
            if (lock != null)
                unlock(lock.readLock());
        }
    }

    @Around("@annotation(unLock)")
    public Object unlock(ProceedingJoinPoint pjp,
                         org.hsweb.concurrent.lock.annotation.UnLock unLock) throws Throwable {
        String name = getLockName(pjp);
        Lock lock = lockMap.get(name);
        try {
            return pjp.proceed();
        } finally {
            logger.debug("unlock :{}", name);
            unlock(lock);
        }
    }

    @Around("@annotation(lock)")
    public Object lock(ProceedingJoinPoint pjp,
                       org.hsweb.concurrent.lock.annotation.Lock lock) throws Throwable {
        String name = getLockName(pjp);
        Lock _lock = lockMap.get(name);
        if (_lock == null) {
            synchronized (lockMap) {
                lockMap.put(name, _lock = lockFactory.createLock(name));
            }
        }
        try {
            logger.debug("try lock :{}", name);
            boolean locked = _lock.tryLock(lock.waitTime(), lock.timeUnit());
            if (!locked) throw new LockException(name + "error");
            return pjp.proceed();
        } finally {
            if (lock.autoUnLock()) {
                logger.debug("unlock :{}", name);
                unlock(_lock);
            }
        }
    }

    @Around("@annotation(lock)")
    public Object readLock(ProceedingJoinPoint pjp,
                           org.hsweb.concurrent.lock.annotation.ReadLock lock) throws Throwable {
        String name = getLockName(pjp);
        ReadWriteLock readWriteLock = readWriteLockMap.get(name);
        if (readWriteLock == null) {
            synchronized (readWriteLockMap) {
                readWriteLockMap.put(name, readWriteLock = lockFactory.createReadWriteLock(name));
            }
        }
        Lock readLock = readWriteLock.readLock();
        try {
            logger.debug("try readLock :{} ", name);
            boolean locked = readLock.tryLock(lock.waitTime(), lock.timeUnit());
            if (!locked) throw new LockException(name + "error");
            return pjp.proceed();
        } finally {
            if (lock.autoUnLock()) {
                logger.debug("unlock readLock :{} ", name);
                unlock(readLock);
            }
        }
    }

    @Around("@annotation(lock)")
    public Object writeLock(ProceedingJoinPoint pjp,
                            org.hsweb.concurrent.lock.annotation.WriteLock lock) throws Throwable {
        String name = getLockName(pjp);
        ReadWriteLock readWriteLock = readWriteLockMap.get(name);
        if (readWriteLock == null) {
            synchronized (readWriteLockMap) {
                readWriteLockMap.put(name, readWriteLock = lockFactory.createReadWriteLock(name));
            }
        }
        Lock writeLock = readWriteLock.writeLock();
        try {
            logger.debug("try writeLock :{} ", name);
            boolean locked = writeLock.tryLock(lock.waitTime(), lock.timeUnit());
            if (!locked) throw new LockException(name + "error");
            return pjp.proceed();
        } finally {
            if (lock.autoUnLock()) {
                logger.debug("unlock writeLock:{} ", name);
                unlock(writeLock);
            }
        }
    }

    public String getLockName(ProceedingJoinPoint pjp) throws Throwable {
        String lockNameStr;
        MethodSignature methodSignature = ((MethodSignature) pjp.getSignature());
        LockName lockName =
                methodSignature.getMethod().getAnnotation(LockName.class);
        if (lockName == null)
            lockName = pjp.getTarget().getClass().getAnnotation(LockName.class);
        if (lockName == null) {
            lockNameStr = pjp.getTarget().getClass().getName();
        } else {
            if (lockName.isExpression()) {
                String expression = lockName.value();
                String expressionId = String.valueOf(expression.hashCode());
                DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(lockName.expressionLanguage());
                boolean compiled = engine.compiled(expressionId);
                if (!compiled) {
                    engine.compile(expressionId, expression);
                }
                Map<String, Object> var = new HashMap<>();
                String paramNames[] = methodSignature.getParameterNames();
                for (int i = 0; i < paramNames.length; i++) {
                    var.put(paramNames[i], pjp.getArgs()[i]);
                }
                if (expressionScopeBeanMap != null) var.putAll(expressionScopeBeanMap);
                ExecuteResult result = engine.execute(expressionId, var);
                if (result.getException() != null) throw result.getException();
                lockNameStr = result.getResult().toString();
            } else {
                lockNameStr = lockName.value();
            }
        }
        return lockNameStr;
    }

    private void unlock(Lock lock) {
        if (lock != null) {
            try {
                lock.unlock();
            } catch (Throwable e) {
                logger.error("unlock error", e);
            }
        }
    }

}
