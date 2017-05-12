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
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationHolder;
import org.hswebframework.web.authorization.Role;
import org.hswebframework.web.authorization.listener.AuthorizationListener;
import org.hswebframework.web.authorization.listener.event.AuthorizationSuccessEvent;

import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
public class ListenerAuthorizingRealm extends AuthorizingRealm
        implements AuthorizationListener<AuthorizationSuccessEvent> {

    public ListenerAuthorizingRealm() {
        setAuthenticationTokenClass(SimpleAuthenticationToken.class);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String loginUserId = (String) super.getAvailablePrincipal(principals);
        return createAuthorizationInfo(AuthenticationHolder.get(loginUserId));
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (token instanceof SimpleAuthenticationToken) {
            return createAuthenticationInfo(((SimpleAuthenticationToken) token).getAuthentication());
        }
        throw new AuthenticationException(new UnsupportedOperationException("{token_un_supported}"));
    }

    private AuthenticationInfo createAuthenticationInfo(Authentication authentication) {
        return new SimpleAuthenticationInfo(
                authentication.getUser().getId(),
                authentication.getUser().getUsername(),
                ListenerAuthorizingRealm.class.getName());
    }

    public void loginOut(Authentication authentication) {
        SecurityUtils.getSubject().logout();
    }

    protected AuthorizationInfo createAuthorizationInfo(Authentication authentication) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.addRoles(authentication.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
        authorizationInfo.addObjectPermissions(
                authentication.getPermissions()
                        .stream()
                        .map(permission -> {
                            String builder = permission.getId() + permission.getActions().stream()
                                    .reduce((a1, a2) -> a1.concat(",").concat(a2))
                                    .orElse("");
                            return new WildcardPermission(builder);
                        }).collect(Collectors.toList()));

        return authorizationInfo;
    }

    @Override
    public void on(AuthorizationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        boolean remember = Boolean.valueOf((String) event.getParameter("remember").orElse("false"));
        Subject subject = SecurityUtils.getSubject();
        subject.login(new SimpleAuthenticationToken(authentication, remember));
    }

}
