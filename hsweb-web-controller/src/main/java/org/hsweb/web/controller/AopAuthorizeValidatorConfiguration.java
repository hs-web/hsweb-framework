package org.hsweb.web.controller;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hsweb.web.authorize.AopAuthorizeValidator;
import org.hsweb.web.exception.AuthorizeException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by zhouhao on 16-4-28.
 */
@Aspect
@Component
@Order(1)
public class AopAuthorizeValidatorConfiguration extends AopAuthorizeValidator {
    @Around(value = "execution(* org.hsweb.web.controller..*Controller..*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        boolean access = super.validate(pjp);
        if (!access) throw new AuthorizeException("无权限", 403);
        return pjp.proceed();
    }
}
