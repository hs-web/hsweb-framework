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

import org.hswebframework.web.authorization.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleMultiAuthentication implements MultiAuthentication {

    private Set<String> authenticationStore = new HashSet<>(4);

    private String activeUserId;

    @Override
    public User getUser() {
        return AuthenticationHolder.get(activeUserId).getUser();
    }

    @Override
    public List<Role> getRoles() {
        return AuthenticationHolder.get(activeUserId).getRoles();
    }

    @Override
    public List<Permission> getPermissions() {
        return AuthenticationHolder.get(activeUserId).getPermissions();
    }

    @Override
    public <T extends Serializable> Optional<T> getAttribute(String name) {
        return AuthenticationHolder.get(activeUserId).getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Serializable object) {
        AuthenticationHolder.get(activeUserId).setAttribute(name, object);
    }

    @Override
    public void setAttributes(Map<String, Serializable> attributes) {
        AuthenticationHolder.get(activeUserId).setAttributes(attributes);
    }

    @Override
    public <T extends Serializable> T removeAttributes(String name) {
        return AuthenticationHolder.get(activeUserId).removeAttributes(name);
    }

    @Override
    public Map<String, Serializable> getAttributes() {
        return AuthenticationHolder.get(activeUserId).getAttributes();
    }

    @Override
    public Set<Authentication> getAuthentications() {
        return authenticationStore.stream()
                .map(AuthenticationHolder::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Authentication activate(String userId) {
        if (!authenticationStore.contains(userId)) return null;
        this.activeUserId = userId;
        return AuthenticationHolder.get(userId);
    }

    @Override
    public void addAuthentication(Authentication authentication) {
        if (activeUserId == null) {
            activeUserId = authentication.getUser().getId();
        }
        authenticationStore.add(authentication.getUser().getId());
    }
}
