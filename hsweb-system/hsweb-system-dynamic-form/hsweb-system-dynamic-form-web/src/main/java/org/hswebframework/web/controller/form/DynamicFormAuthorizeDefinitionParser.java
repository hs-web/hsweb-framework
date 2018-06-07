package org.hswebframework.web.controller.form;

import org.hswebframework.web.AopUtils;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.basic.aop.AopMethodAuthorizeDefinitionCustomizerParser;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

/**
 * @author zhouhao
 * @since 3.0
 */
public abstract class DynamicFormAuthorizeDefinitionParser implements AopMethodAuthorizeDefinitionCustomizerParser {
    @Override
    public AuthorizeDefinition parse(Class target, Method method, MethodInterceptorContext context) {
        if (!ClassUtils.getUserClass(target).equals(DynamicFormOperationController.class)
                || context == null) {
            return null;
        }
        Authorize methodAuth = AopUtils.findMethodAnnotation(target, method, Authorize.class);

        //获取表单id
        return context.<String>getParameter("formId")
                .map(formId -> getDefinition(formId, methodAuth == null ? new String[0] : methodAuth.action(), context))
                .orElse(null);
    }

    protected abstract AuthorizeDefinition getDefinition(String formId, String[] action, MethodInterceptorContext context);
}
