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

package org.hswebframework.web.commons.entity.factory;

import org.hswebframework.web.commons.entity.Entity;

/**
 * 实体工厂接口,系统各个地方使用此接口来创建实体,在实际编码中也应该使用此接口来创建实体,而不是使用new方式来创建
 *
 * @author zhouhao
 * @see Entity
 * @see MapperEntityFactory
 * @since 3.0
 */
public interface EntityFactory {
    /**
     * 根据类型创建实例
     * <p>
     * e.g.
     * <pre>
     *  entityFactory.newInstance(UserEntity.class);
     * </pre>
     *
     * @param entityClass 要创建的class
     * @param <T>         类型
     * @return 创建结果
     */
    <T> T newInstance(Class<T> entityClass);


    /**
     * 根据类型创建实例,如果类型无法创建,则使用默认类型进行创建
     * <p>
     * e.g.
     * <pre>
     *  entityFactory.newInstance(UserEntity.class,SimpleUserEntity.class);
     * </pre>
     *
     * @param entityClass  要创建的class
     * @param defaultClass 默认class,当{@code entityClass}无法创建时使用此类型进行创建
     * @param <T>          类型
     * @return 实例
     */
    <T> T newInstance(Class<T> entityClass, Class<? extends T> defaultClass);

    /**
     * 创建实体并设置默认的属性
     *
     * @param entityClass       实体类型
     * @param defaultProperties 默认属性
     * @param <S>               默认属性的类型
     * @param <T>               实体类型
     * @return 创建结果
     * @see EntityFactory#copyProperties(Object, Object)
     */
    default <S, T> T newInstance(Class<T> entityClass, S defaultProperties) {
        return copyProperties(defaultProperties, newInstance(entityClass));
    }

    /**
     * 创建实体并设置默认的属性
     *
     * @param entityClass       实体类型
     * @param defaultClass      默认class
     * @param defaultProperties 默认属性
     * @param <S>               默认属性的类型
     * @param <T>               实体类型
     * @return 创建结果
     * @see EntityFactory#copyProperties(Object, Object)
     */
    default <S, T> T newInstance(Class<T> entityClass, Class<? extends T> defaultClass, S defaultProperties) {
        return copyProperties(defaultProperties, newInstance(entityClass, defaultClass));
    }


    /**
     * 根据类型获取实体的真实的实体类型,
     * 可通过此方法获取获取已拓展的实体类型，如:<br>
     * <code>
     * factory.getInstanceType(MyBeanInterface.class);
     * </code>
     *
     * @param entityClass 类型
     * @param <T>         泛型
     * @return 实体类型
     */
    <T> Class<T> getInstanceType(Class<T> entityClass);

    /**
     * 拷贝对象的属性
     *
     * @param source 要拷贝到的对象
     * @param target 被拷贝的对象
     * @param <S>    要拷贝对象的类型
     * @param <T>    被拷贝对象的类型
     * @return 被拷贝的对象
     */
    <S, T> T copyProperties(S source, T target);
}
