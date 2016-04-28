package org.hsweb.web.controller;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hsweb.web.bean.po.logger.LoggerInfo;
import org.hsweb.web.exception.BusinessException;
import org.hsweb.web.logger.AccessLoggerPersisting;
import org.hsweb.web.logger.AopAccessLoggerResolver;
import org.hsweb.web.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.webbuilder.utils.common.StringUtils;

import java.util.List;

/**
 * Created by zhouhao on 16-4-28.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AopAccessLoggerResolverConfiguration extends AopAccessLoggerResolver {

    @Autowired
    private FastJsonHttpMessageConverter fastJsonHttpMessageConverter;

    @Autowired(required = false)
    private List<AccessLoggerPersisting> accessLoggerPersisting;

    @Around(value = "execution(* org.hsweb.web.controller..*Controller..*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        LoggerInfo loggerInfo = resolver(pjp);
        long requestTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = pjp.proceed();
        } catch (Throwable e) {
            result = new ResponseMessage(false, e);
            if (!(e instanceof BusinessException))
                loggerInfo.setException_info(StringUtils.throwable2String(e));
            throw e;
        } finally {
            long responseTime = System.currentTimeMillis();
            loggerInfo.setRequest_time(requestTime);
            loggerInfo.setResponse_time(responseTime);
            loggerInfo.setResponse_content(fastJsonHttpMessageConverter.converter(result));
            if (accessLoggerPersisting != null) {
                accessLoggerPersisting.forEach(loggerPersisting -> loggerPersisting.save(loggerInfo));
            }
        }
        return result;
    }
}
