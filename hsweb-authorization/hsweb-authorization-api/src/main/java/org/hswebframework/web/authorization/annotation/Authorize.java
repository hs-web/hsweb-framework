/*
 *
 *  * Copyright 2019 http://www.hswebframework.org
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

import org.hswebframework.web.authorization.define.Phased;

import java.lang.annotation.*;

/**
 * 基础权限控制注解,提供基本的控制配置
 *
 * @author zhouhao
 * @see org.hswebframework.web.authorization.Authentication
 * @see org.hswebframework.web.authorization.define.AuthorizeDefinition
 * @see Resource
 * @see ResourceAction
 * @see Dimension
 * @see DataAccess
 * @since 3.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Authorize {

    Resource[] resources() default {};

    Dimension[] dimension() default {};

    /**
     * 验证失败时返回的消息
     *
     * @return 验证失败提示的消息
     */
    String message() default "无访问权限";

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
     * @return 验证时机，在方法调用前还是调用后s
     */
    Phased phased() default Phased.before;

    /**
     * @return 是否忽略, 忽略后将不进行权限控制
     */
    boolean ignore() default false;


    String[] description() default {};
}
