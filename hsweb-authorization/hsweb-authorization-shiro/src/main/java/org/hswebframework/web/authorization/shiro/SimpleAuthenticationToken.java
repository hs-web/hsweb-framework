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
import org.hswebframework.web.authorization.Authentication;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleAuthenticationToken implements AuthenticationToken, HostAuthenticationToken, RememberMeAuthenticationToken {
    private Authentication authentication;

    private boolean rememberMe;

    private String host;

    public SimpleAuthenticationToken(Authentication authentication, boolean rememberMe) {
        this.authentication = authentication;
        this.rememberMe = rememberMe;
    }

    @Override
    public Object getPrincipal() {
        return authentication.getUser().getId();
    }

    @Override
    public Object getCredentials() {
        return authentication.getUser().getUsername();
    }

    public Authentication getAuthentication() {
        return authentication;
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
