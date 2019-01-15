/*
 *  Copyright 2019 http://www.hswebframework.org
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

package org.hswebframework.web.authorization.listener.event;

import java.util.function.Function;

/**
 * 授权前事件
 *
 * @author zhouhao
 * @since 3.0
 */
public class AuthorizationBeforeEvent extends AbstractAuthorizationEvent {

    private static final long serialVersionUID = 5948747533500518524L;

    public AuthorizationBeforeEvent(String username, String password, Function<String, Object> parameterGetter) {
        super(username, password, parameterGetter);
    }
}
