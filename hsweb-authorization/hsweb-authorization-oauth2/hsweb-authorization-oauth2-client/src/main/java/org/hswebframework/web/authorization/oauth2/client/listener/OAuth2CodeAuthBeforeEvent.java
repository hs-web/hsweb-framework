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

package org.hswebframework.web.authorization.oauth2.client.listener;


import org.springframework.context.ApplicationEvent;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author zhouhao
 */
public class OAuth2CodeAuthBeforeEvent extends ApplicationEvent implements OAuth2Event {
    private static final long serialVersionUID = -2106764405363442985L;
    private String                   code;
    private String                   state;
    private Function<String, String> parameterGetter;

    public OAuth2CodeAuthBeforeEvent(String code, String state, Function<String, String> parameterGetter) {
        super(code);
        this.code = code;
        this.state = state;
        this.parameterGetter = parameterGetter;
    }

    public String getCode() {
        return code;
    }

    public String getState() {
        return state;
    }

    public Optional<String> getParameter(String name) {
        return Optional.ofNullable(parameterGetter.apply(name));
    }

}
