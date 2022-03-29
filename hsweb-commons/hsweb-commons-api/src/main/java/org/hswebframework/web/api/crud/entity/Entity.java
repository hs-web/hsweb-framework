/*
 *
 *  * Copyright 2020 http://www.hswebframework.org
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

package org.hswebframework.web.api.crud.entity;


import org.hswebframework.ezorm.core.StaticMethodReferenceColumn;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.validator.ValidatorUtils;

import java.io.Serializable;

/**
 * 实体总接口,所有实体需实现此接口
 *
 * @author zhouhao
 * @since 3.0
 */
public interface Entity extends Serializable {

    /**
     * 使用jsr303对当前实体类进行验证，如果未通过验证则会抛出{@link org.hswebframework.web.exception.ValidationException}异常
     *
     * @param groups 分组
     * @see org.hswebframework.web.exception.ValidationException
     */
    default void tryValidate(Class<?>... groups) {
        ValidatorUtils.tryValidate(this, groups);
    }

    /**
     * 使用jsr303对当前实体类的指定属性进行验证，如果未通过验证则会抛出{@link org.hswebframework.web.exception.ValidationException}异常
     *
     * @param groups 分组
     * @see org.hswebframework.web.exception.ValidationException
     */
    default void tryValidate(String property, Class<?>... groups) {
        ValidatorUtils.tryValidate(this, property, groups);
    }

    /**
     * 使用jsr303对当前实体类的指定属性进行验证，如果未通过验证则会抛出{@link org.hswebframework.web.exception.ValidationException}异常
     *
     * @param groups 分组
     * @see org.hswebframework.web.exception.ValidationException
     */
    default void tryValidate(StaticMethodReferenceColumn<?> property, Class<?>... groups) {
        tryValidate(property.getColumn(), groups);
    }

    /**
     * 将当前实体类复制到指定其他类型中,类型将会被自动实例化,在类型明确时,建议使用{@link Entity#copyFrom(Object, String...)}.
     *
     * @param target           目标类型
     * @param ignoreProperties 忽略复制的属性
     * @param <T>类型
     * @return 复制结果
     */
    default <T> T copyTo(Class<T> target, String... ignoreProperties) {
        return FastBeanCopier.copy(this, target, ignoreProperties);
    }

    /**
     * 将当前实体类复制到其他对象中
     *
     * @param target           目标实体
     * @param ignoreProperties 忽略复制的属性
     * @param <T>类型
     * @return 复制结果
     */
    default <T> T copyTo(T target, String... ignoreProperties) {
        return FastBeanCopier.copy(this, target, ignoreProperties);
    }

    /**
     * 从其他对象复制属性到当前对象
     *
     * @param target           其他对象
     * @param ignoreProperties 忽略复制的属性
     * @param <T>              类型
     * @return 当前对象
     */
    @SuppressWarnings("all")
    default <T> T copyFrom(Object target, String... ignoreProperties) {
        return (T) FastBeanCopier.copy(target, this, ignoreProperties);
    }
}
