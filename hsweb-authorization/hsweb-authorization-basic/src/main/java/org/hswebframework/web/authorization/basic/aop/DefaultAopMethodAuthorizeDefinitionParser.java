package org.hswebframework.web.authorization.basic.aop;

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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注解权限控制定义解析器,通过判断方法上的注解来获取权限控制的方式
 *
 * @author zhouhao
 * @see AopMethodAuthorizeDefinitionParser
 * @see AuthorizeDefinition
 */

public class DefaultAopMethodAuthorizeDefinitionParser implements AopMethodAuthorizeDefinitionParser {

    private Map<CacheKey, AuthorizeDefinition> cache = new ConcurrentHashMap<>();


    private List<AopMethodAuthorizeDefinitionCustomizerParser> parserCustomers;

    @Autowired(required = false)
    public void setParserCustomers(List<AopMethodAuthorizeDefinitionCustomizerParser> parserCustomers) {
        this.parserCustomers = parserCustomers;
    }

    @Override
    public AuthorizeDefinition parse(MethodInterceptorContext paramContext) {
        CacheKey key = buildCacheKey(paramContext);

        AuthorizeDefinition definition = cache.get(key);
        if (definition != null && (definition instanceof EmptyAuthorizeDefinition)) {
            return null;
        }
        //使用自定义
        if (!CollectionUtils.isEmpty(parserCustomers)) {
            definition = parserCustomers.stream()
                    .map(customer -> customer.parse(paramContext))
                    .filter(Objects::nonNull)
                    .findAny().orElse(null);
            if (definition == null || definition instanceof EmptyAuthorizeDefinition) {
                return null;
            }

        }

        Authorize classAuth = AopUtils.findAnnotation(paramContext.getTarget().getClass(), Authorize.class);
        Authorize methodAuth = AopUtils.findMethodAnnotation(paramContext.getTarget().getClass(), paramContext.getMethod(), Authorize.class);
        RequiresDataAccess classDataAccess = AopUtils.findAnnotation(paramContext.getTarget().getClass(), RequiresDataAccess.class);
        RequiresDataAccess methodDataAccess = AopUtils.findMethodAnnotation(paramContext.getTarget().getClass(), paramContext.getMethod(), RequiresDataAccess.class);

        RequiresExpression expression = AopUtils.findAnnotation(paramContext.getTarget().getClass(), RequiresExpression.class);

        if (classAuth == null && methodAuth == null && classDataAccess == null && methodDataAccess == null && expression == null) {
            cache.put(key, EmptyAuthorizeDefinition.instance);
            return null;
        }

        if ((methodAuth != null && methodAuth.ignore()) || (classAuth != null && classAuth.ignore())) {
            cache.put(key, EmptyAuthorizeDefinition.instance);
            return null;
        }

        DefaultBasicAuthorizeDefinition authorizeDefinition = new DefaultBasicAuthorizeDefinition();

        if (methodAuth == null || methodAuth.merge())
            authorizeDefinition.put(classAuth);

        authorizeDefinition.put(methodAuth);

        authorizeDefinition.put(expression);

        authorizeDefinition.put(classDataAccess);

        authorizeDefinition.put(methodDataAccess);

        cache.put(key, authorizeDefinition);
        return authorizeDefinition;
    }

    public CacheKey buildCacheKey(MethodInterceptorContext context) {
        return new CacheKey(ClassUtils.getUserClass(context.getTarget()), context.getMethod());
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

}
