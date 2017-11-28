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

package org.hswebframework.web.authorization.listener.event;

import org.hswebframework.web.authorization.Authentication;
import org.springframework.context.ApplicationEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 授权成功事件,当授权成功时,触发此事件,并传入授权的信息
 *
 * @author zhouhao
 * @see Authentication
 * @since 3.0
 */
public class AuthorizationSuccessEvent extends ApplicationEvent implements AuthorizationEvent {
    private static final long serialVersionUID = -2452116314154155058L;
    private Authentication authentication;

    private transient Function<String, Object> parameterGetter;

    private Map<String, Object> result = new HashMap<>();

    public AuthorizationSuccessEvent(Authentication authentication, Function<String, Object> parameterGetter) {
        super(authentication);
        this.authentication = authentication;
        this.parameterGetter = parameterGetter;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getParameter(String name) {
        return Optional.ofNullable((T) parameterGetter.apply(name));
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }
}
