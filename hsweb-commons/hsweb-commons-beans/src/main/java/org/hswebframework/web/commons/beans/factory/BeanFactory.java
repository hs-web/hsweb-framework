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

package org.hswebframework.web.commons.beans.factory;

import org.hswebframework.web.commons.beans.Bean;

/**
 * 实体工厂接口,系统各个地方使用此接口来创建实体,在实际编码中也应该使用此接口来创建实体,而不是使用new方式来创建
 *
 * @author zhouhao
 * @see Bean
 * @since 3.0
 */
public interface BeanFactory {
    /**
     * 根据类型创建实体,类型必须为{@link Bean}的子类。
     *
     * @param beanClass 类型
     * @param <T>       泛型,需实现{@link Bean}
     * @return 实体
     */
    <T extends Bean> T newInstance(Class<T> beanClass);

    /**
     * 根据类型获取实体的真实的实体类型,
     * 可通过此方法获取获取已拓展的实体类型，如:<br>
     * <code>
     * factory.getInstanceType(MyBeanInterface.class); -> class SimpleMyBeanInterface
     * </code>
     *
     * @param beanClass 类型
     * @param <T>       泛型
     * @return 实体类型
     */
    <T extends Bean> Class<T> getInstanceType(Class<T> beanClass);
}
