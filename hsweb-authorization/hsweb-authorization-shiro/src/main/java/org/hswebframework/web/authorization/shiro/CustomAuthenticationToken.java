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

package org.hswebframework.web.authorization.shiro;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.HostAuthenticationToken;
import org.apache.shiro.authc.RememberMeAuthenticationToken;
import org.hswebframework.web.authorization.Authorization;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class CustomAuthenticationToken implements AuthenticationToken, HostAuthenticationToken, RememberMeAuthenticationToken {
    private Authorization authorization;

    private boolean rememberMe;

    private String host;

    public CustomAuthenticationToken(Authorization authorization, boolean rememberMe) {
        this.authorization = authorization;
        this.rememberMe = rememberMe;
    }

    @Override
    public Object getPrincipal() {
        return authorization.getUser().getUsername();
    }

    @Override
    public Object getCredentials() {
        return authorization.getUser().getId();
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    @Override
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public boolean isRememberMe() {
        return rememberMe;
    }
}
