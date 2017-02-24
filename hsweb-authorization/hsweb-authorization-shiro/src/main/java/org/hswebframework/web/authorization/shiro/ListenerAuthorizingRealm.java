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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.hswebframework.web.authorization.Authorization;
import org.hswebframework.web.authorization.Role;
import org.hswebframework.web.authorization.listener.UserAuthorizationListener;

import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
public class ListenerAuthorizingRealm extends AuthorizingRealm implements UserAuthorizationListener {

    public ListenerAuthorizingRealm() {
        setAuthenticationTokenClass(CustomAuthenticationToken.class);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String loginName = (String) super.getAvailablePrincipal(principals);
        return this.<String, AuthorizationInfo>getCache(loginName)
                .get(AuthorizationInfo.class.getName());
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (token instanceof CustomAuthenticationToken) {
            return this.<String, AuthenticationInfo>getCache((String) token.getPrincipal())
                    .get(AuthenticationInfo.class.getName());
        }
        throw new AuthenticationException(new UnsupportedOperationException("{token_un_supported}"));
    }

    private AuthenticationInfo createAuthenticationInfo(Authorization authorization) {
        return new SimpleAuthenticationInfo(
                authorization.getUser().getUsername(),
                authorization.getUser().getId(),
                authorization.getUser().getName());
    }

    @Override
    public void onLoginOut(Authorization authorization) {
        if (null != authorization)
            getCache(authorization.getUser().getUsername()).clear();
        SecurityUtils.getSubject().logout();
    }

    @Override
    public void onAuthorizeSuccess(boolean isRemembered, Authorization authorization) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.addRoles(authorization.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
        authorizationInfo.addObjectPermissions(
                authorization.getPermissions()
                        .stream()
                        .map(permission -> {
                            StringBuilder builder = new StringBuilder(permission.getId());
                            builder.append(permission.getActions().stream()
                                    .reduce((a1, a2) -> a1.concat(",").concat(a2))
                                    .orElse(""));
                            return new WildcardPermission(builder.toString());
                        }).collect(Collectors.toList()));

        getCache(authorization.getUser().getUsername())
                .put(AuthorizationInfo.class.getName(), authorizationInfo);

        getCache(authorization.getUser().getUsername())
                .put(AuthenticationInfo.class.getName(), createAuthenticationInfo(authorization));

        Subject subject = SecurityUtils.getSubject();
        subject.login(new CustomAuthenticationToken(authorization, isRemembered));
        subject.getSession().setAttribute(Authorization.class.getName(), authorization);
    }

    protected <K, V> Cache<K, V> getCache(String name) {
        return getCacheManager().getCache(getCacheName(name));
    }

    protected String getCacheName(String name) {
        return "shiro.auth.info.".concat(name);
    }
}
