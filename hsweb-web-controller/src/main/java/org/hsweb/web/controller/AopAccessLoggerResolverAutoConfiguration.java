package org.hsweb.web.controller;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hsweb.web.bean.po.logger.LoggerInfo;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.core.logger.AccessLoggerPersisting;
import org.hsweb.web.core.logger.AopAccessLoggerResolver;
import org.hsweb.web.core.message.FastJsonHttpMessageConverter;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.core.utils.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.hsweb.commons.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by zhouhao on 16-4-28.
 */
@Configuration
@ConditionalOnProperty(name = "logger.access", havingValue = "true")
public class AopAccessLoggerResolverAutoConfiguration{
    @Bean
    public AopAccessLoggerResolverConfiguration aopAccessLoggerResolverConfiguration() {
        return new AopAccessLoggerResolverConfiguration();
    }

    @Aspect
    @Order(Ordered.HIGHEST_PRECEDENCE)
    static class AopAccessLoggerResolverConfiguration extends org.hsweb.web.core.logger.AopAccessLoggerResolver {
        @Autowired(required = false)
        private FastJsonHttpMessageConverter fastJsonHttpMessageConverter;

        @Autowired(required = false)
        private List<AccessLoggerPersisting> accessLoggerPersisting;

        @PostConstruct
        private void init() {
            if (fastJsonHttpMessageConverter == null)
                fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        }

        @Around(value = "execution(* org.hsweb.web..controller..*Controller..*(..))||@annotation(org.hsweb.web.core.logger.annotation.AccessLogger)")
        public Object around(ProceedingJoinPoint pjp) throws Throwable {
            LoggerInfo loggerInfo = resolver(pjp);
            long requestTime = System.currentTimeMillis();
            Object result = null;
            try {
                result = pjp.proceed();
            } catch (Throwable e) {
                if (!(e instanceof BusinessException)) {
                    result = ResponseMessage.error(e.getMessage());
                    loggerInfo.setExceptionInfo(StringUtils.throwable2String(e));
                } else {
                    result = ResponseMessage.error(e.getMessage(), ((BusinessException) e).getStatus());
                }
                throw e;
            } finally {
                long responseTime = System.currentTimeMillis();
                User user = WebUtil.getLoginUser();
                loggerInfo.setRequestTime(requestTime);
                loggerInfo.setResponseTime(responseTime);
                loggerInfo.setResponseContent(fastJsonHttpMessageConverter.converter(result));
                if (user != null)
                    loggerInfo.setUserId(user.getId());
                if (result instanceof ResponseMessage)
                    loggerInfo.setResponseCode(String.valueOf(((ResponseMessage) result).getCode()));
                if (accessLoggerPersisting != null) {
                    accessLoggerPersisting.forEach(loggerPersisting -> loggerPersisting.save(loggerInfo));
                }
            }
            return result;
        }
    }

}
