package org.hsweb.web.logger;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.hsweb.web.bean.po.logger.LoggerInfo;
import org.hsweb.web.logger.annotation.AccessLogger;
import org.hsweb.web.utils.WebUtil;
import org.webbuilder.utils.common.ClassUtils;
import org.webbuilder.utils.common.MD5;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * Created by zhouhao on 16-4-28.
 */
public class AopAccessLoggerResolver {

    public LoggerInfo resolver(ProceedingJoinPoint pjp) {
        LoggerInfo logInfo = new LoggerInfo();
        HttpServletRequest request = WebUtil.getHttpServletRequest();
        Class<?> target = pjp.getTarget().getClass();
        StringBuilder describe = new StringBuilder();
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        String methodName = getMethodName(pjp);

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
        logInfo.setU_id(MD5.encode(String.valueOf(System.nanoTime())));
        logInfo.setModule_desc(describe.toString());//方法描述
        logInfo.setClass_name(target.getName());//当前访问映射到的类名
        logInfo.setClient_ip(WebUtil.getIpAddr(request));//ip地址
        logInfo.setRequest_method(request.getMethod().concat(".").concat(methodName));//方法：GET.select()
        logInfo.setRequest_header(JSON.toJSONString(WebUtil.getHeaders(request)));//http请求头
        logInfo.setReferer(request.getHeader("referer"));//referer
        logInfo.setRequest_uri(request.getRequestURI());//请求相对路径
        logInfo.setRequest_url(WebUtil.getBasePath(request).concat(logInfo.getRequest_uri().substring(1)));//请求绝对路径
        logInfo.setUser_agent(request.getHeader("User-agent"));//客户端标识
        logInfo.setRequest_param(JSON.toJSONString(WebUtil.getParams(request)));//请求参数
        return logInfo;
    }

    protected String getMethodName(ProceedingJoinPoint pjp) {
        StringBuilder methodName = new StringBuilder(pjp.getSignature().getName()).append("(");
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String[] names = signature.getParameterNames();
        Class[] args = signature.getParameterTypes();
        for (int i = 0, len = args.length; i < len; i++) {
            if (i != 0) methodName.append(",");
            methodName.append(args[i].getSimpleName()).append(" ").append(names[i]);
        }
        return methodName.append(")").toString();
    }

}
