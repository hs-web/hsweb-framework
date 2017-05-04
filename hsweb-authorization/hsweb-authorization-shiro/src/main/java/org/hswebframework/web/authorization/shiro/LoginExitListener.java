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

package org.hswebframework.web.authorization.shiro;

import org.hswebframework.web.authorization.listener.AuthorizationListener;
import org.hswebframework.web.authorization.listener.event.AuthorizationExitEvent;

/**
 *
 * @author zhouhao
 */
public class LoginExitListener implements AuthorizationListener<AuthorizationExitEvent> {

    private ListenerAuthorizingRealm listenerAuthorizingRealm;

    public LoginExitListener(ListenerAuthorizingRealm listenerAuthorizingRealm) {
        this.listenerAuthorizingRealm = listenerAuthorizingRealm;
    }

    @Override
    public void on(AuthorizationExitEvent event) {
        listenerAuthorizingRealm.loginOut(event.getAuthentication());
    }
}
