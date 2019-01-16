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

import org.hswebframework.web.authorization.Authentication;
import org.springframework.context.ApplicationEvent;

/**
 * 退出登录事件
 *
 * @author zhouhao
 */
public class AuthorizationExitEvent extends ApplicationEvent implements AuthorizationEvent {

    private static final long serialVersionUID = -4590245933665047280L;

    private Authentication authentication;

    public AuthorizationExitEvent(Authentication authentication) {
        super(authentication);
        this.authentication = authentication;
    }

    public Authentication getAuthentication() {
        return authentication;
    }
}
