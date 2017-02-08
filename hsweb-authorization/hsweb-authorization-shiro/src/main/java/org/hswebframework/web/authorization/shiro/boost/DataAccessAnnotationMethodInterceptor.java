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

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler;
import org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.authorization.Authorization;
import org.hswebframework.web.authorization.AuthorizationHolder;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccess;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.access.ParamContext;
import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.authorization.annotation.RequiresDataAccess;
import org.hswebframework.web.authorization.annotation.RequiresExpression;
import org.hswebframwork.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DataAccessAnnotationMethodInterceptor extends AuthorizingAnnotationMethodInterceptor {

    public DataAccessAnnotationMethodInterceptor(DataAccessController controller) {
        super(new DataAccessAnnotationHandler(controller));
    }

    private static final Logger logger = LoggerFactory.getLogger(DataAccessAnnotationMethodInterceptor.class);

    static class DataAccessAnnotationHandler extends AuthorizingAnnotationHandler {
        protected DataAccessController dataAccessController;

        public DataAccessAnnotationHandler(DataAccessController controller) {
            super(RequiresDataAccess.class);
            this.dataAccessController = controller;
        }

        Map<Class<DataAccessController>, DataAccessController> cache = new HashMap<>();

        @Override
        public void assertAuthorized(Annotation a) throws AuthorizationException {
            if (!(a instanceof RequiresDataAccess)) return;
            MethodInterceptorHolder holder = MethodInterceptorHolder.current();
            if (null == holder) {
                logger.warn("MethodInterceptorHolder is null!");
                return;
            }
            RequiresDataAccess accessAnn = ((RequiresDataAccess) a);
            DataAccessController accessController = dataAccessController;
            if (DataAccessController.class != accessAnn.controllerClass()) {
                if (null == (accessController = cache.get(accessAnn.controllerClass()))) {
                    synchronized (cache) {
                        if (null == (accessController = cache.get(accessAnn.controllerClass())))
                            try {
                                accessController = accessAnn.controllerClass().newInstance();
                                cache.put(accessAnn.controllerClass(), accessController);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                    }
                }
            } else if (StringUtils.isNullOrEmpty(accessAnn.controllerBeanName())) {
                // TODO: 17-2-8  get controller from spring context
            }
            DataAccessController finalAccessController = accessController;

            ParamContext context = holder.createParamContext(accessAnn);
            Authorization authorization = AuthorizationHolder.get();
            if (authorization == null) {
                throw new AuthorizationException("{no_authorization}");
            }
            String permission = accessAnn.permission();
            Permission permissionInfo = authorization.getPermission(permission);
            List<String> actionList = Arrays.asList(accessAnn.action());

            Set<DataAccess> accesses = permissionInfo
                    .getDataAccesses()
                    .stream()
                    .filter(access -> actionList.contains(access.getAction()))
                    .collect(Collectors.toSet());
            if (accesses.isEmpty()) return;
            Function<Predicate<DataAccess>, Boolean> function =
                    (accessAnn.logical() == Logical.AND) ?
                            accesses.stream()::allMatch : accesses.stream()::anyMatch;

            boolean isAccess = function.apply(access -> finalAccessController.doAccess(access, context));
            if (!isAccess) {
                throw new AuthorizationException("{access_deny}");
            }
        }
    }
}
