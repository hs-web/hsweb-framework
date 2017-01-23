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

import java.lang.annotation.*;

/**
 * 权限注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Authorize {

    /**
     * 对角色授权,当使用按角色授权时，对模块以及操作级别授权方式失效
     *
     * @return 进行授权的角色数组
     */
    String[] role() default {};

    /**
     * 对模块授权
     *
     * @return 进行授权的模块
     */
    String[] module() default {};

    /**
     * 如增删改查等
     *
     * @return
     */
    String[] action() default {};

    /**
     * 验证是否为指定user
     *
     * @return
     */
    String[] user() default {};

    /**
     * 验证失败时返回的消息
     *
     * @return
     */
    String message() default "{unauthorized}";

    /**
     * 表达式验证
     *
     * @return
     */
    String expression() default "";

    /**
     * 表达式语言，默认spring表达式
     *
     * @return 表达式语言
     */
    String expressionLanguage() default "spel";

    /**
     * 是否为api接口，为true时，可使用api方式请求。
     *
     * @return
     */
    boolean api() default false;

    /**
     * 可匿名访问
     *
     * @return 是否可匿名访问, 匿名访问将不用登录
     */
    boolean anonymous() default false;

    /**
     * 是否合并类上的注解
     *
     * @return 是否合并类上的注解
     */
    boolean merge() default true;

    /**
     * 验证模式，在使用多个验证条件时有效
     *
     * @return
     */
    Logical logical() default Logical.OR;

}
