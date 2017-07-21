/*
 *  Copyright 2016 http://www.hswebframework.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.hswebframework.web.authorization.shiro.boost;

import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler;
import org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor;
import org.hswebframework.utils.ClassUtils;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.ExpressionUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.Role;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 对{@link Authorize} 注解的支持
 *
 * @author zhouhao
 */
public class SimpleAuthorizeMethodInterceptor extends AuthorizingAnnotationMethodInterceptor {
    public SimpleAuthorizeMethodInterceptor(AnnotationResolver resolver) {
        super(new AuthorizeAnnotationHandler(), resolver);
    }

    private static final Logger logger = LoggerFactory.getLogger(SimpleAuthorizeMethodInterceptor.class);


    static class AuthorizeAnnotationHandler extends AuthorizingAnnotationHandler {

        public AuthorizeAnnotationHandler() {
            super(Authorize.class);
        }

        @Override
        public void assertAuthorized(Annotation a) throws AuthorizationException {
            if (!(a instanceof Authorize)) return;
            MethodInterceptorHolder holder = MethodInterceptorHolder.current();
            if (null == holder) {
                logger.warn("MethodInterceptorHolder is null,maybe config is error!");
                return;
            }
            AuthorizeConfig authorizeConfig = new AuthorizeConfig(holder.getArgs());
            Authorize authorize = ((Authorize) a);
            if (authorize.ignore()) return;

            if (authorize.merge()) {
                Authorize classAnn = ClassUtils.getAnnotation(holder.getTarget().getClass(), Authorize.class);
                if (null != classAnn) {
                    if (classAnn.ignore()) return;
                    authorizeConfig.put(classAnn);
                }
            }
            authorizeConfig.put(authorize);

            Authentication authentication = Authentication.current()
                    .orElseThrow(() -> new UnauthenticatedException(authorizeConfig.message));
            boolean access = true;
            Logical logical = authorizeConfig.logical == Logical.DEFAULT ? Logical.OR : authorizeConfig.logical;
            boolean logicalIsOr = logical == Logical.OR;
            // 控制权限
            if (!authorizeConfig.permission.isEmpty()) {
                List<Permission> permissions = authentication.getPermissions().stream()
                        .filter(permission -> {
                            // 未持有任何一个权限
                            if (!authorizeConfig.permission.contains(permission.getId())) return false;
                            //未配置action
                            if (authorizeConfig.action.isEmpty())
                                return true;
                            //判断action
                            List<String> actions = permission.getActions()
                                    .stream()
                                    .filter(authorizeConfig.action::contains)
                                    .collect(Collectors.toList());
                            
                            if (actions.isEmpty()) return false;

                            //如果 控制逻辑是or,则只要过滤结果数量不为0.否则过滤结果数量必须和配置的数量相同
                            return logicalIsOr ? actions.size() > 0 : permission.getActions().containsAll(actions);
                        }).collect(Collectors.toList());
                access = logicalIsOr ?
                        permissions.size() > 0 :
                        //权限数量和配置的数量相同
                        permissions.size() == authorizeConfig.permission.size();
            }
            //控制角色
            if (!authorizeConfig.role.isEmpty()) {
                Function<Predicate<Role>, Boolean> func = logicalIsOr
                        ? authentication.getRoles().stream()::anyMatch
                        : authentication.getRoles().stream()::allMatch;
                access = func.apply(role -> authorizeConfig.role.contains(role.getId()));
            }
            //控制用户
            if (!authorizeConfig.user.isEmpty()) {
                Function<Predicate<String>, Boolean> func = logicalIsOr
                        ? authorizeConfig.user.stream()::anyMatch
                        : authorizeConfig.user.stream()::allMatch;
                access = func.apply(authentication.getUser().getUsername()::equals);
            }
            if (!access) {
                throw new AuthorizationException(authorizeConfig.message);
            }
        }
    }

    static class AuthorizeConfig {
        Set<String>         permission = new LinkedHashSet<>();
        Set<String>         action     = new LinkedHashSet<>();
        Set<String>         role       = new LinkedHashSet<>();
        Set<String>         user       = new LinkedHashSet<>();
        Logical             logical    = Logical.DEFAULT;
        String              message    = "unauthorized";
        Map<String, Object> var        = null;

        public AuthorizeConfig(Map<String, Object> var) {
            this.var = var;
        }

        public void put(Authorize authorize) {
            permission.addAll(tryCompileExpression(authorize.permission()));
            action.addAll(tryCompileExpression(authorize.action()));
            role.addAll(tryCompileExpression(authorize.role()));
            user.addAll(tryCompileExpression(authorize.user()));
            if (!StringUtils.isNullOrEmpty(authorize.message())) {
                message = tryCompileExpression(authorize.message());
            }
            if (authorize.logical() != Logical.DEFAULT)
                logical = authorize.logical();
        }

        public String tryCompileExpression(String express) {
            try {
                return ExpressionUtils.analytical(express, var, "spel");
            } catch (Exception e) {
                throw new AuthorizationException("系统错误", e);
            }
//            if (express.startsWith("${") && express.endsWith("}")) {
//                express = express.substring(2, express.length() - 1);
//                DynamicScriptEngine spelEngine = DynamicScriptEngineFactory.getEngine("spel");
//                String id = DigestUtils.md5Hex(express);
//                try {
//                    if (!spelEngine.compiled(id))
//                        spelEngine.compile(id, express);
//                    return String.valueOf(spelEngine.execute(id, var).getIfSuccess());
//                } catch (Exception e) {
//                    throw new AuthorizationException("系统错误", e);
//                } finally {
//                    //     spelEngine.remove(id);
//                }
//            } else {
//                return express;
//            }
        }

        public Collection<String> tryCompileExpression(String... expresses) {
            return Arrays.stream(expresses)
                    .filter(Objects::nonNull)
                    .map(this::tryCompileExpression)
                    .collect(Collectors.toSet());
        }
    }
}