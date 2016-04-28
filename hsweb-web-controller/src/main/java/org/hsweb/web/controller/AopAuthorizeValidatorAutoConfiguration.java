package org.hsweb.web.controller;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hsweb.web.authorize.AopAuthorizeValidator;
import org.hsweb.web.exception.AuthorizeException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Created by zhouhao on 16-4-28.
 */
@Configuration
@ConditionalOnProperty(name = "login.enable", havingValue = "true")
public class AopAuthorizeValidatorAutoConfiguration extends AopAuthorizeValidator {

    @Bean
    public ControllerAuthorizeValidator controllerAuthorizeValidator() {
        return new ControllerAuthorizeValidator();
    }

    @Aspect
    @Order(1)
    static class ControllerAuthorizeValidator extends AopAuthorizeValidator {
        @Around(value = "execution(* org.hsweb.web.controller..*Controller..*(..))")
        public Object around(ProceedingJoinPoint pjp) throws Throwable {
            boolean access = super.validate(pjp);
            if (!access) throw new AuthorizeException("无权限", 403);
            return pjp.proceed();
        }
    }
}
