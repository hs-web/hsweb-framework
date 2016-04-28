package org.hsweb.web.authorize;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.hsweb.web.authorize.annotation.Authorize;
import org.hsweb.web.authorize.validator.SimpleAuthorizeValidator;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.exception.AuthorizeException;
import org.hsweb.web.utils.WebUtil;
import org.webbuilder.utils.common.ClassUtils;
import org.webbuilder.utils.common.StringUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhouhao on 16-4-28.
 */
public class AopAuthorizeValidator extends SimpleAuthorizeValidator {

    protected ConcurrentMap<String, AuthorizeValidatorConfig> configCache = new ConcurrentHashMap<>();

    protected AuthorizeValidatorConfig getConfig(ProceedingJoinPoint pjp) {
        String cacheKey = StringUtils.concat(pjp.getTarget().getClass().getName(), ".", getMethodName(pjp));
        AuthorizeValidatorConfig config = configCache.get(cacheKey);
        if (config == null) {
            config = this.createConfig();
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Authorize methodAuth = ClassUtils.getAnnotation(signature.getMethod(), Authorize.class);
            Authorize classAuth = ClassUtils.getAnnotation(pjp.getTarget().getClass(), Authorize.class);
            if (methodAuth == null && classAuth == null) {
                return null;
            }
            Set<Authorize> authorizes = new LinkedHashSet<>();
            if (classAuth != null)
                authorizes.add(classAuth);
            if (methodAuth != null)
                authorizes.add(methodAuth);

            config.addAnnotation(authorizes);
            configCache.put(cacheKey, config);
        }
        return config;
    }

    public boolean validate(ProceedingJoinPoint pjp) {
        AuthorizeValidatorConfig config = getConfig(pjp);
        if (config == null) return true;
        User user = WebUtil.getLoginUser();
        if (user == null) throw new AuthorizeException("未登录", 401);
        Map<String, Object> param = new LinkedHashMap<>();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String[] names = signature.getParameterNames();
        for (int i = 0; i < names.length; i++) {
            param.put(names[i], pjp.getArgs()[i]);
        }
        return validate(user, param, config);
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
