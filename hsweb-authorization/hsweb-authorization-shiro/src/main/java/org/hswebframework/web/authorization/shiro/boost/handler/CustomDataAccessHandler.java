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

package org.hswebframework.web.authorization.shiro.boost.handler;

import org.hswebframework.web.authorization.access.*;
import org.hswebframework.web.boost.aop.context.MethodInterceptorParamContext;

/**
 * 当配置为自定义处理器时(实现{@link CustomDataAccess }接口),此处理器生效
 *
 * @author zhouhao
 * @see 3.0
 */
public class CustomDataAccessHandler implements DataAccessHandler {

    @Override
    public boolean isSupport(DataAccessConfig access) {
        return access instanceof CustomDataAccess;
    }

    @Override
    public boolean handle(DataAccessConfig access, MethodInterceptorParamContext context) {
        CustomDataAccess custom = ((CustomDataAccess) access);
        return custom.getController().doAccess(access, context);
    }
}
