/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.authorization.annotation;

import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.Role;
import org.hswebframework.web.authorization.User;

import java.lang.annotation.*;

/**
 * 基础权限控制注解,提供基本的控制配置
 *
 * @author zhouhao
 * @see org.hswebframework.web.authorization.Authentication
 * @see org.hswebframework.web.authorization.define.AuthorizeDefinition
 * @since 3.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface  Authorize {

    /**
     * 对角色授权,当使用按角色授权时，对模块以及操作级别授权方式失效
     *
     * @return 进 role id array
     * @see Role#getId()
     */
    String[] role() default {};

    /**
     * 对模块授权
     *
     * @return permission id array
     * @see Permission#getId()
     */
    String[] permission() default {};

    /**
     * 如增删改查等
     *
     * @return action array
     * @see Permission#getActions()
     */
    String[] action() default {};

    /**
     * 验证是否为指定user
     *
     * @return username array
     * @see User#getUsername()
     */
    String[] user() default {};

    /**
     * 验证失败时返回的消息
     *
     * @return 验证失败提示的消息
     */
    String message() default "{unauthorized}";

    /**
     * 是否合并类上的注解
     *
     * @return 是否合并类上的注解
     */
    boolean merge() default true;

    /**
     * 验证模式，在使用多个验证条件时有效
     *
     * @return logical
     */
    Logical logical() default Logical.DEFAULT;

    /**
     * @return 是否忽略, 忽略后将不进行权限控制
     */
    boolean ignore() default false;

    /**
     *
     * @return 数据权限控制
     */
    RequiresDataAccess[] dataAccess()default {};
}
