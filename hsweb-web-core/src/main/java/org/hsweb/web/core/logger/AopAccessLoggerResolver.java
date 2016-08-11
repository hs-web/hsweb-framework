package org.hsweb.web.core.logger;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.hsweb.commons.DateTimeUtils;
import org.hsweb.web.bean.po.logger.LoggerInfo;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.utils.AopUtils;
import org.hsweb.web.core.utils.WebUtil;
import org.hsweb.commons.ClassUtils;
import org.hsweb.commons.MD5;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhouhao on 16-4-28.
 */
public class AopAccessLoggerResolver {

    public LoggerInfo resolver(ProceedingJoinPoint pjp) {
        LoggerInfo logInfo = new LoggerInfo();
        HttpServletRequest request = WebUtil.getHttpServletRequest();
        Class<?> target = pjp.getTarget().getClass();
        StringBuilder describe = new StringBuilder();
        MethodSignature methodSignature = ((MethodSignature) pjp.getSignature());
        Method method = methodSignature.getMethod();
        String methodName = AopUtils.getMethodName(pjp);
        AccessLogger classAnnotation = ClassUtils.getAnnotation(target, AccessLogger.class);
        AccessLogger methodAnnotation = ClassUtils.getAnnotation(method, AccessLogger.class);
        if (classAnnotation != null) {
            describe.append(classAnnotation.value());
        }
        if (methodAnnotation != null) {
            if (classAnnotation != null)
                describe.append("-");
            describe.append(methodAnnotation.value());
        }
        Map<String, Object> param = new LinkedHashMap<>();
        String[] paramNames = methodSignature.getParameterNames();
        Object[] args = pjp.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            Object arg = args[i];
            String argString;
            if (arg instanceof HttpServletRequest
                    || arg instanceof HttpSession
                    || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile
                    || arg instanceof MultipartFile[]) continue;
            if (arg instanceof String) argString = (String) arg;
            else if (arg instanceof Number) argString = String.valueOf(arg);
            else if (arg instanceof Date) argString = DateTimeUtils.format(((Date) arg), DateTimeUtils.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND);
            else {
                try {
                    argString = JSON.toJSONString(arg);
                } catch (Exception e) {
                    continue;
                }
            }
            param.put(paramNames[i], argString);
        }
        Map<String, String> header = WebUtil.getHeaders(request);
        logInfo.setId(MD5.encode(String.valueOf(System.nanoTime())));
        logInfo.setModuleDesc(describe.toString());//方法描述
        logInfo.setClassName(target.getName());//当前访问映射到的类名
        logInfo.setClientIp(WebUtil.getIpAddr(request));//ip地址
        logInfo.setRequestMethod(request.getMethod().concat(".").concat(methodName));//方法：GET.select()
        logInfo.setRequestHeader(JSON.toJSONString(header));//http请求头
        logInfo.setReferer(header.get("Referer"));//referer
        logInfo.setRequestUri(request.getRequestURI());//请求相对路径
        logInfo.setRequestUrl(WebUtil.getBasePath(request).concat(logInfo.getRequestUri().substring(1)));//请求绝对路径
        logInfo.setUserAgent(header.get("User-Agent"));//客户端标识
        logInfo.setRequestParam(JSON.toJSONString(param));//请求参数
        return logInfo;
    }



}
