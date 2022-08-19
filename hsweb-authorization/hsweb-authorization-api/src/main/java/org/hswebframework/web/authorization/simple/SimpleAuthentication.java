/*
 * Copyright 2020 http://www.hswebframework.org
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

package org.hswebframework.web.authorization.simple;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.*;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
public class SimpleAuthentication implements Authentication {

    private static final long serialVersionUID = -2898863220255336528L;

    private User user;

    private List<Permission> permissions = new ArrayList<>();

    private List<Dimension> dimensions = new ArrayList<>();

    private Map<String, Serializable> attributes = new HashMap<>();

    public static Authentication of() {
        return new SimpleAuthentication();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> Optional<T> getAttribute(String name) {
        return Optional.ofNullable((T) attributes.get(name));
    }

    @Override
    public Map<String, Serializable> getAttributes() {
        return attributes;
    }

    public SimpleAuthentication merge(Authentication authentication) {
        Map<String, Permission> mePermissionGroup = permissions
                .stream()
                .collect(Collectors.toMap(Permission::getId, Function.identity()));

        if (authentication.getUser() != null) {
            user = authentication.getUser();
        }

        attributes.putAll(authentication.getAttributes());

        for (Permission permission : authentication.getPermissions()) {
            Permission me = mePermissionGroup.get(permission.getId());
            if (me == null) {
                permissions.add(permission.copy());
                continue;
            }
            me.getActions().addAll(permission.getActions());
            me.getDataAccesses().addAll(permission.getDataAccesses());
        }

        for (Dimension dimension : authentication.getDimensions()) {
            if (!getDimension(dimension.getType(), dimension.getId()).isPresent()) {
                dimensions.add(dimension);
            }
        }
        return this;
    }

    @Override
    public Authentication copy(BiPredicate<Permission, String> permissionFilter,
                               Predicate<Dimension> dimension) {
        SimpleAuthentication authentication = new SimpleAuthentication();
        authentication.setDimensions(dimensions.stream().filter(dimension).collect(Collectors.toList()));
        authentication.setPermissions(permissions
                                              .stream()
                                              .map(permission -> permission.copy(action -> permissionFilter.test(permission, action), conf -> true))
                                              .filter(per -> !per.getActions().isEmpty())
                                              .collect(Collectors.toList())
        );
        authentication.setUser(user);
        return authentication;
    }

    public void setUser(User user) {
        this.user = user;
        dimensions.add(user);
    }

    public void setDimensions(List<Dimension> dimensions) {
        this.dimensions.addAll(dimensions);
    }

    public void addDimension(Dimension dimension) {
        this.dimensions.add(dimension);
    }
}
