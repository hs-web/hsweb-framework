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

import org.apache.shiro.SecurityUtils;
import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.authorization.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 可自动同步权限信息的权限获取器,当修改了权限信息后。
 * 如:{@link Authentication#setAttribute}会自动将修改后的数据同步到权限存储中
 *
 * @author zhouhao
 * @see AuthenticationSupplier
 * @see AuthenticationManager
 * @since 3.0
 */
public class AutoSyncAuthenticationSupplier implements AuthenticationSupplier {
    private AuthenticationManager authenticationManager;

    public AutoSyncAuthenticationSupplier(AuthenticationManager authenticationManager) {
        Objects.requireNonNull(authenticationManager);
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication get(String userId) {
        Authentication nativeAuth = getNative(userId);
        if (null == nativeAuth) return null;
        return new AutoSyncAuthentication(nativeAuth);
    }

    @Override
    public Authentication get() {
        Authentication nativeAuth = getNative();
        if (null == nativeAuth) return null;
        return new AutoSyncAuthentication(nativeAuth);
    }

    protected Authentication getNative(String userId) {
        // ThreadLocal cache
        return ThreadLocalUtils.get(Authentication.class.getName(), () -> authenticationManager.getByUserId(userId));
    }

    protected Authentication getNative() {
        //未授权并且未记住登录
        if (!SecurityUtils.getSubject().isAuthenticated() && !SecurityUtils.getSubject().isRemembered()) return null;
        String id = (String) SecurityUtils.getSubject().getPrincipal();
        if (null == id) return null;
        return getNative(id);
    }

    protected void sync(Authentication authentication) {
        authenticationManager.sync(authentication);
    }

    class AutoSyncAuthentication implements Authentication {
        private Authentication nativeAuth;

        public AutoSyncAuthentication(Authentication nativeAuth) {
            this.nativeAuth = nativeAuth;
        }

        @Override
        public User getUser() {
            return nativeAuth.getUser();
        }

        @Override
        public List<Role> getRoles() {
            return nativeAuth.getRoles();
        }

        @Override
        public List<Permission> getPermissions() {
            return nativeAuth.getPermissions();
        }

        @Override
        public <T extends Serializable> Optional<T> getAttribute(String name) {
            return nativeAuth.getAttribute(name);
        }

        @Override
        public void setAttribute(String name, Serializable object) {
            nativeAuth.setAttribute(name, object);
            sync(nativeAuth);
        }

        @Override
        public void setAttributes(Map<String, Serializable> attributes) {
            nativeAuth.setAttributes(attributes);
            sync(nativeAuth);
        }

        @Override
        public <T extends Serializable> T removeAttributes(String name) {
            T t = nativeAuth.removeAttributes(name);
            sync(nativeAuth);
            return t;
        }

        @Override
        public Map<String, Serializable> getAttributes() {
            return nativeAuth.getAttributes();
        }
    }
}
