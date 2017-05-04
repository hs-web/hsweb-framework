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

package org.hswebframework.web.authorization.annotation;

import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.FieldAccessConfig;

import java.lang.annotation.*;

/**
 * 字段级权限控制注解,用于进行需要字段级别权限控制的声明.
 * <p>
 * 此注解仅用于声明此方法需要进行字段级权限控制,具体权限控制方式由控制器实{@link org.hswebframework.web.authorization.access.FieldAccessController}现
 * </p>
 *
 * @author zhouhao
 * @see org.hswebframework.web.authorization.access.FieldAccessController
 * @since 3.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresFieldAccess {

    /**
     * @return permission id
     * @see Permission#getId()
     */
    String permission();

    /**
     * @return action
     * @see FieldAccessConfig#getActions()
     */
    String action();

    Logical logical() default Logical.OR;

    String paramName() default "";

}
