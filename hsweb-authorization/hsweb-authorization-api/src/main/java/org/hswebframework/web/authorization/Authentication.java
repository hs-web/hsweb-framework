/*
 * Copyright 2019 http://www.hswebframework.org
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

package org.hswebframework.web.authorization;

import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户授权信息,当前登录用户的权限信息,包括用户的基本信息,角色,权限集合等常用信息<br>
 * 获取方式:
 * <ul>
 * <li>springmvc 入参方式: ResponseMessage myTest(Authorization auth){}</li>
 * <li>静态方法方式:AuthorizationHolder.get();</li>
 * </ul>
 *
 * @author zhouhao
 * @see ReactiveAuthenticationHolder
 * @see AuthenticationManager
 * @since 3.0
 */
public interface Authentication extends Serializable {

    /**
     * 获取当前登录的用户权限信息
     * <pre>
     *     public Mono&lt;User&gt; getUser(){
     *         return Authentication.currentReactive()
     *                 .switchIfEmpty(Mono.error(new UnAuthorizedException()))
     *                 .flatMap(autz->findUserByUserId(autz.getUser().getId()));
     *     }
     * </pre>
     *
     * @return 当前用户权限信息
     * @see ReactiveAuthenticationHolder
     */
    static Mono<Authentication> currentReactive() {
        return ReactiveAuthenticationHolder.get();
    }

    /**
     * 非响应式环境适用
     * <pre>
     *
     *   Authentication auth= Authentication.current().get();
     *   //如果权限信息不存在将抛出{@link NoSuchElementException}建议使用下面的方式获取
     *   Authentication auth=Authentication.current().orElse(null);
     *   //或者
     *   Authentication auth=Authentication.current().orElseThrow(UnAuthorizedException::new);
     * </pre>
     *
     * @return 当前用户权限信息
     * @see Optional
     */
    static Optional<Authentication> current() {
        return AuthenticationHolder.get();
    }

    /**
     * @return 用户信息
     */
    User getUser();

    /**
     * @return 用户所有维度
     */
    List<Dimension> getDimensions();

    /**
     * @return 用户持有的权限集合
     */
    List<Permission> getPermissions();


    default boolean hasDimension(String type, String... id) {
        return hasDimension(type, Arrays.asList(id));
    }

    default boolean hasDimension(String type, Collection<String> id) {
        if (id.isEmpty()) {
            return !getDimensions(type).isEmpty();
        }
        return getDimensions(type)
                .stream()
                .anyMatch(p -> id.contains(p.getId()));
    }

    default boolean hasDimension(DimensionType type, String id) {
        return getDimension(type, id).isPresent();
    }

    default Optional<Dimension> getDimension(String type, String id) {
        if (StringUtils.isEmpty(type)) {
            return Optional.empty();
        }
        return getDimensions()
                .stream()
                .filter(dimension -> dimension.getId().equals(id) && type.equalsIgnoreCase(dimension.getType().getId()))
                .findFirst();
    }

    default Optional<Dimension> getDimension(DimensionType type, String id) {
        if (type == null) {
            return Optional.empty();
        }
        return getDimensions()
                .stream()
                .filter(dimension -> dimension.getId().equals(id) && type.isSameType(dimension.getType()))
                .findFirst();
    }


    default List<Dimension> getDimensions(String type) {
        if (StringUtils.isEmpty(type)) {
            return Collections.emptyList();
        }
        return getDimensions()
                .stream()
                .filter(dimension -> dimension.getType().isSameType(type))
                .collect(Collectors.toList());
    }

    default List<Dimension> getDimensions(DimensionType type) {
        if (type == null) {
            return Collections.emptyList();
        }
        return getDimensions()
                .stream()
                .filter(dimension -> dimension.getType().isSameType(type))
                .collect(Collectors.toList());
    }


    /**
     * 根据权限id获取权限信息,权限不存在则返回null
     *
     * @param id 权限id
     * @return 权限信息
     */
    default Optional<Permission> getPermission(String id) {
        if (null == id) {
            return Optional.empty();
        }
        return getPermissions().stream()
                .filter(permission -> permission.getId().equals(id))
                .findAny();
    }

    /**
     * 判断是否持有某权限以及对权限的可操作事件
     *
     * @param permissionId 权限id {@link Permission#getId()}
     * @param actions      可操作事件 {@link Permission#getActions()} 如果为空,则不判断action,只判断permissionId
     * @return 是否持有权限
     */
    default boolean hasPermission(String permissionId, String... actions) {
        return hasPermission(permissionId, Arrays.asList(actions));
    }

    default boolean hasPermission(String permissionId, Collection<String> actions) {
        return getPermission(permissionId)
                .filter(permission -> actions.isEmpty() || permission.getActions().containsAll(actions))
                .isPresent();
    }

    /**
     * 根据属性名获取属性值,返回一个{@link Optional}对象。<br>
     * 此方法可用于获取自定义的属性信息
     *
     * @param name 属性名
     * @param <T>  属性值类型
     * @return Optional属性值
     */
    <T extends Serializable> Optional<T> getAttribute(String name);

    /**
     * @return 全部属性集合
     */
    Map<String, Serializable> getAttributes();

    Authentication merge(Authentication source);

}
