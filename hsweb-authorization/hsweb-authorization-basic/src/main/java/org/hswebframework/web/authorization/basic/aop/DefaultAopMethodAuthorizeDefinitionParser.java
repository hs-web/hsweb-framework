package org.hswebframework.web.authorization.basic.aop;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.AopUtils;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.RequiresDataAccess;
import org.hswebframework.web.authorization.annotation.RequiresExpression;
import org.hswebframework.web.authorization.basic.define.DefaultBasicAuthorizeDefinition;
import org.hswebframework.web.authorization.basic.define.EmptyAuthorizeDefinition;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注解权限控制定义解析器,通过判断方法上的注解来获取权限控制的方式
 *
 * @author zhouhao
 * @see AopMethodAuthorizeDefinitionParser
 * @see AuthorizeDefinition
 */
@Slf4j
public class DefaultAopMethodAuthorizeDefinitionParser implements AopMethodAuthorizeDefinitionParser {

    private Map<CacheKey, AuthorizeDefinition> cache = new ConcurrentHashMap<>();

    private List<AopMethodAuthorizeDefinitionCustomizerParser> parserCustomizers;

    private static Set<String> excludeMethodName = new HashSet<>(Arrays.asList("toString", "clone", "hashCode", "getClass"));

    @Autowired(required = false)
    public void setParserCustomizers(List<AopMethodAuthorizeDefinitionCustomizerParser> parserCustomizers) {
        this.parserCustomizers = parserCustomizers;
    }

    @Override
    public List<AuthorizeDefinition> getAllParsed() {
        return new ArrayList<>(cache.values());
    }

    @Override
    @SuppressWarnings("all")
    public AuthorizeDefinition parse(Class target, Method method, MethodInterceptorContext context) {
        if (excludeMethodName.contains(method.getName())) {
            return null;
        }
        CacheKey key = buildCacheKey(target, method);

        AuthorizeDefinition definition = cache.get(key);
        if (definition instanceof EmptyAuthorizeDefinition) {
            return null;
        }
        if (null != definition) {
            return definition;
        }
        //使用自定义
        if (!CollectionUtils.isEmpty(parserCustomizers)) {
            definition = parserCustomizers.stream()
                    .map(customizer -> customizer.parse(target, method, context))
                    .filter(Objects::nonNull)
                    .findAny().orElse(null);
            if (definition instanceof EmptyAuthorizeDefinition) {
                return null;
            }
            if (definition != null) {
                return definition;
            }
        }
        Authorize classAuth = AopUtils.findAnnotation(target, Authorize.class);
        Authorize methodAuth = AopUtils.findMethodAnnotation(target, method, Authorize.class);

        RequiresDataAccess classDataAccess = AopUtils.findAnnotation(target, RequiresDataAccess.class);

        RequiresDataAccess methodDataAccess = AopUtils.findMethodAnnotation(target, method, RequiresDataAccess.class);

        RequiresExpression expression = AopUtils.findAnnotation(target, RequiresExpression.class);

        if (classAuth == null && methodAuth == null && classDataAccess == null && methodDataAccess == null && expression == null) {
            cache.put(key, EmptyAuthorizeDefinition.instance);
            return null;
        }

        if ((methodAuth != null && methodAuth.ignore()) || (classAuth != null && classAuth.ignore())) {
            cache.put(key, EmptyAuthorizeDefinition.instance);
            return null;
        }
        DefaultBasicAuthorizeDefinition authorizeDefinition = new DefaultBasicAuthorizeDefinition();
        authorizeDefinition.setTargetClass(target);
        authorizeDefinition.setTargetMethod(method);
        if (methodAuth == null || methodAuth.merge()) {
            authorizeDefinition.put(classAuth);
        }

        authorizeDefinition.put(methodAuth);

        authorizeDefinition.put(expression);

        authorizeDefinition.put(classDataAccess);

        authorizeDefinition.put(methodDataAccess);

        if (authorizeDefinition.getPermissionDescription().length == 0) {
            if (classAuth != null) {
                authorizeDefinition.put(classAuth.dataAccess());
                String[] desc = classAuth.description();
                if (desc.length > 0) {
                    authorizeDefinition.setPermissionDescription(desc);
                }
            }
        }

        if (authorizeDefinition.getActionDescription().length == 0) {
            if (methodAuth != null) {
                if (methodAuth.description().length != 0) {
                    authorizeDefinition.setActionDescription(methodAuth.description());
                }
            }
        }

        log.info("parsed authorizeDefinition {}.{} => {}.{} permission:{} actions:{}",
                target.getSimpleName(),
                method.getName(),
                authorizeDefinition.getPermissionDescription(),
                authorizeDefinition.getActionDescription(),
                authorizeDefinition.getPermissions(),
                authorizeDefinition.getActions());
        cache.put(key, authorizeDefinition);
        return authorizeDefinition;
    }

    public CacheKey buildCacheKey(Class target, Method method) {
        return new CacheKey(ClassUtils.getUserClass(target), method);
    }

    class CacheKey {
        private Class  type;
        private Method method;

        public CacheKey(Class type, Method method) {
            this.type = type;
            this.method = method;
        }

        @Override
        public int hashCode() {
            return Arrays.asList(type, method).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && this.hashCode() == obj.hashCode();
        }
    }

    public void destroy() {
        cache.clear();
    }

}
