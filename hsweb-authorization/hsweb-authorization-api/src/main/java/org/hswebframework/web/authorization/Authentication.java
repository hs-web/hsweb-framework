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

import java.io.Serializable;
import java.util.*;

/**
 * 用户授权信息,当前登录用户的权限信息,包括用户的基本信息,角色,权限集合等常用信息<br>
 * 获取方式:
 * <ul>
 * <li>springmvc 入参方式: ResponseMessage myTest(Authorization auth){}</li>
 * <li>静态方法方式:AuthorizationHolder.get();</li>
 * </ul>
 *
 * @author zhouhao
 * @see AuthenticationHolder
 * @see AuthenticationManager
 * @since 3.0
 */
public interface Authentication extends Serializable {

    /**
     * 获取当前登录的用户权限信息
     * <pre>
     *
     *   Authentication auth= Authentication.current().get();
     *   //如果权限信息不存在将抛出{@link NoSuchElementException}建议使用下面的方式获取
     *   Authentication auth=Authentication.current().orElse(null);
     *   //或者
     *   Authentication auth=Authentication.current().orElseThrow(UnAuthorizedException::new);
     * </pre>
     *
     * @return 返回Optional对象进行操作
     * @see Optional
     * @see AuthenticationHolder
     */
    static Optional<Authentication> current() {
        return Optional.ofNullable(AuthenticationHolder.get());
    }

    /**
     * @return 用户信息
     */
    User getUser();

    /**
     * @return 用户持有的角色集合
     */
    List<Role> getRoles();

    /**
     * @return 用户持有的权限集合
     */
    List<Permission> getPermissions();

    /**
     * 根据id获取角色,角色不存在则返回null
     *
     * @param id 角色id
     * @return 角色信息
     */
    default Optional<Role> getRole(String id) {
        if (null == id) {
            return Optional.empty();
        }
        return getRoles().stream()
                .filter(role -> role.getId().equals(id))
                .findAny();
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
        return getPermission(permissionId)
                .filter(permission -> actions.length == 0 || permission.getActions().containsAll(Arrays.asList(actions)))
                .isPresent();
    }

    /**
     * @param roleId 角色id {@link Role#getId()}
     * @return 是否拥有某个角色
     */
    default boolean hasRole(String roleId) {
        return getRole(roleId).isPresent();
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
     * 设置一个属性值,如果属性名称已经存在,则将其覆盖。<br>
     * 注意:由于权限信息可能会被序列化,属性值必须实现{@link Serializable}接口
     *
     * @param name   属性名称
     * @param object 属性值
     * @see AuthenticationManager#sync(Authentication)
     */
    void setAttribute(String name, Serializable object);

    /**
     * 设置多个属性值,参数为map类型,key为属性名称,value为属性值
     *
     * @param attributes 属性值map
     * @see AuthenticationManager#sync(Authentication)
     */
    void setAttributes(Map<String, Serializable> attributes);

    /**
     * 删除属性,并返回被删除的值
     *
     * @param name 属性名
     * @param <T>  被删除的值类型
     * @return 被删除的值
     * @see AuthenticationManager#sync(Authentication)
     */
    <T extends Serializable> T removeAttributes(String name);

    /**
     * 获取全部属性,此属性为通过{@link this#setAttribute(String, Serializable)}或{@link this#setAttributes(Map)}设置的属性。
     *
     * @return 全部属性集合
     */
    Map<String, Serializable> getAttributes();

}
