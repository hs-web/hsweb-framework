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
import org.hswebframework.web.authorization.Authorization;
import org.hswebframework.web.authorization.AuthorizationHolder;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.FieldAccess;
import org.hswebframework.web.authorization.access.FieldAccessController;
import org.hswebframework.web.authorization.access.ParamContext;
import org.hswebframework.web.authorization.annotation.RequiresDataAccess;
import org.hswebframework.web.authorization.annotation.RequiresFieldAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class FieldAccessAnnotationMethodInterceptor extends AuthorizingAnnotationMethodInterceptor {

    public FieldAccessAnnotationMethodInterceptor(FieldAccessController controller) {
        super(new DataAccessAnnotationHandler(controller));
    }

    private static final Logger logger = LoggerFactory.getLogger(FieldAccessAnnotationMethodInterceptor.class);

    static class DataAccessAnnotationHandler extends AuthorizingAnnotationHandler {
        protected FieldAccessController fieldAccessController;

        public DataAccessAnnotationHandler(FieldAccessController controller) {
            super(RequiresFieldAccess.class);
            this.fieldAccessController = controller;
        }

        @Override
        public void assertAuthorized(Annotation a) throws AuthorizationException {
            if (!(a instanceof RequiresFieldAccess)) return;
            MethodInterceptorHolder holder = MethodInterceptorHolder.current();
            if (null == holder) {
                logger.warn("MethodInterceptorHolder is null!");
                return;
            }
            RequiresFieldAccess accessAnn = ((RequiresFieldAccess) a);
            ParamContext context = holder.createParamContext();
            Authorization authorization = AuthorizationHolder.get();
            if (authorization == null) {
                throw new AuthorizationException("{no_authorization}");
            }
            String permission = accessAnn.permission();
            Permission permissionInfo = authorization.getPermission(permission);

            Set<FieldAccess> accesses = permissionInfo
                    .getFieldAccesses()
                    .stream()
                    .filter(access -> access.getActions().contains(accessAnn.action()))
                    .collect(Collectors.toSet());
            boolean isAccess = fieldAccessController.doAccess(accessAnn.action(), accesses, context);
            if (!isAccess) {
                throw new AuthorizationException("{access_deny}");
            }
        }
    }
}
