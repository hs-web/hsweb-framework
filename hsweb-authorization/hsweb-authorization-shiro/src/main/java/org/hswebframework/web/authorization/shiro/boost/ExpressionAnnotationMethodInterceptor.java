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

import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler;
import org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.RequiresExpression;
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class ExpressionAnnotationMethodInterceptor extends AuthorizingAnnotationMethodInterceptor {
    public ExpressionAnnotationMethodInterceptor() {
        super(new ExpressionAnnotationHandler());
    }

    public ExpressionAnnotationMethodInterceptor(AnnotationResolver resolver) {
        super(new ExpressionAnnotationHandler(), resolver);
    }

    private static final Logger logger = LoggerFactory.getLogger(ExpressionAnnotationMethodInterceptor.class);

    static class ExpressionAnnotationHandler extends AuthorizingAnnotationHandler {

        public ExpressionAnnotationHandler() {
            super(RequiresExpression.class);
        }

        @Override
        public void assertAuthorized(Annotation a) throws AuthorizationException {
            if (!(a instanceof RequiresExpression)) return;
            MethodInterceptorHolder holder = MethodInterceptorHolder.current();
            if (null == holder) {
                return;
            }
            RequiresExpression expression = ((RequiresExpression) a);
            DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(expression.language());
            if (null == engine) {
                throw new AuthorizationException("{unknown_engine}:" + expression.language());
            }
            if (!engine.compiled(holder.getId())) {
                try {
                    engine.compile(holder.getId(), expression.value());
                } catch (Exception e) {
                    logger.error("express compile error", e);
                    throw new BusinessException("{expression_error}");
                }
            }
            Map<String, Object> var = new HashMap<>(holder.getArgs());
            var.put("auth", getSubject().getSession().getAttribute(Authentication.class.getName()));
            Object success = engine.execute(holder.getId(), var).get();
            if (!(success instanceof Boolean) || !((Boolean) success)) {
                throw new AuthorizationException();
            }
        }
    }
}
