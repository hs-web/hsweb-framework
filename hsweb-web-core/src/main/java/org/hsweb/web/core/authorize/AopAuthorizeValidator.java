package org.hsweb.web.core.authorize;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.hsweb.commons.ClassUtils;
import org.hsweb.commons.StringUtils;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.authorize.oauth2.OAuth2Manager;
import org.hsweb.web.core.authorize.validator.SimpleAuthorizeValidator;
import org.hsweb.web.core.exception.AuthorizeException;
import org.hsweb.web.core.session.HttpSessionManager;
import org.hsweb.web.core.utils.AopUtils;
import org.hsweb.web.core.utils.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AopAuthorizeValidator extends SimpleAuthorizeValidator {

    private HttpSessionManager httpSessionManager;

    private OAuth2Manager oAuth2Manager;

    @Autowired
    public void setHttpSessionManager(HttpSessionManager httpSessionManager) {
        this.httpSessionManager = httpSessionManager;
    }

    @Autowired(required = false)
    public void setoAuth2Manager(OAuth2Manager oAuth2Manager) {
        this.oAuth2Manager = oAuth2Manager;
    }

    protected ConcurrentMap<String, AuthorizeValidatorConfig> configCache = new ConcurrentHashMap<>();

    protected AuthorizeValidatorConfig getConfig(ProceedingJoinPoint pjp) {
        String cacheKey = StringUtils.concat(pjp.getTarget().getClass().getName(), ".", AopUtils.getMethodName(pjp));
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
            if (classAuth != null && (methodAuth == null || methodAuth.merge())) {
                if (classAuth.anonymous()) return null;
                authorizes.add(classAuth);
            }
            if (methodAuth != null) {
                if (methodAuth.anonymous()) return null;
                authorizes.add(methodAuth);
            }
            config.addAnnotation(authorizes);
            configCache.put(cacheKey, config);
        }
        return config;
    }

    public boolean validate(ProceedingJoinPoint pjp) {
        AuthorizeValidatorConfig config = getConfig(pjp);
        if (config == null) return true;
        User user = null;
        HttpServletRequest request = WebUtil.getHttpServletRequest();
        //api OAuth2 认证
        if (config.isApiSupport()) {
            if (oAuth2Manager != null) {
                String token = oAuth2Manager.getAccessTokenByRequest(request);
                if (token != null) {
                    user = oAuth2Manager.getUserByAccessToken(token);
                    if (user == null) {
                        throw new AuthorizeException("invalid_token", 401);
                    }
                }
            }
        }
        if (user == null) {
            HttpSession session = request.getSession(false);
            if (session == null) throw new AuthorizeException("未登录", 401);
            user = httpSessionManager.getUserBySessionId(session.getId());
            if (user == null) throw new AuthorizeException("未登录", 401);
        }
        if (config.isEmpty()) return true;
        Map<String, Object> param = new LinkedHashMap<>();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String[] names = signature.getParameterNames();
        Object[] args = pjp.getArgs();
        for (int i = 0; i < names.length; i++) {
            param.put(names[i], args[i]);
        }
        param.put("paramsMap", param);
        return validate(user, param, config);
    }

}
