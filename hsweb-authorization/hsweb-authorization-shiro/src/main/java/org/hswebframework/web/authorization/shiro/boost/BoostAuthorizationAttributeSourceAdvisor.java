/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.authorization.shiro.boost;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.authz.annotation.*;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.aop.SpringAnnotationResolver;
import org.apache.shiro.spring.security.interceptor.AopAllianceAnnotationsAuthorizingMethodInterceptor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.hswebframework.web.AopUtils;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.RequiresDataAccess;
import org.hswebframework.web.authorization.annotation.RequiresExpression;
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author zhouhao
 * @see AuthorizationAttributeSourceAdvisor
 * @see StaticMethodMatcherPointcutAdvisor
 */
public class BoostAuthorizationAttributeSourceAdvisor extends StaticMethodMatcherPointcutAdvisor {
    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] AUTHZ_ANNOTATION_CLASSES =
            new Class[]{
                    RequiresPermissions.class,
                    RequiresRoles.class,
                    RequiresUser.class,
                    RequiresGuest.class,
                    RequiresAuthentication.class,
                    //自定义
                    Authorize.class
            };

    protected SecurityManager securityManager = null;

    /**
     * Create a new AuthorizationAttributeSourceAdvisor.
     *
     * @param dataAccessController 数据权限控制器
     */
    public BoostAuthorizationAttributeSourceAdvisor(DataAccessController dataAccessController) {
        AopAllianceAnnotationsAuthorizingMethodInterceptor interceptor =
                new AopAllianceAnnotationsAuthorizingMethodInterceptor() {
                    @Override
                    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                        MethodInterceptorHolder.create(methodInvocation).set();
                        return super.invoke(methodInvocation);
                    }
                };
        AnnotationResolver resolver = new SpringAnnotationResolver();
        setAdvice(interceptor);
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    public void setSecurityManager(org.apache.shiro.mgt.SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    public boolean matches(Method method, Class targetClass) {
        Authorize authorize = AopUtils.findMethodAnnotation(targetClass, method, Authorize.class);
        if (null != authorize) {
            if (authorize.ignore()) return false;
        }
        authorize = AopUtils.findAnnotation(targetClass, Authorize.class);
        if (null != authorize) {
            if (authorize.ignore()) return false;
        }
        return Arrays.stream(AUTHZ_ANNOTATION_CLASSES)
                .anyMatch(aClass -> AopUtils.findAnnotation(targetClass, method, aClass) != null);
    }
}
