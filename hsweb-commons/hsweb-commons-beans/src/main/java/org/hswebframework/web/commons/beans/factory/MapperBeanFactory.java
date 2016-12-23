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

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since 3.0
 */
public class MapperBeanFactory implements BeanFactory {
    private Map<Class, Class> realTypeMapper = new HashMap<>();

    @Override
    public <T extends Bean> T getInstance(Class<T> beanClass) {
        Class<T> realType = getRealType(beanClass);
        if (realType == null) {
            if (!Modifier.isInterface(beanClass.getModifiers()) && !Modifier.isAbstract(beanClass.getModifiers())) {
                realType = beanClass;
            }
        }
        //尝试使用 Simple类，如: package.SimpleUserBean
        if (realType == null) {
            String simpleClassName = beanClass.getPackage().getName().concat(".Simple").concat(beanClass.getSimpleName());
            try {
                realType = (Class<T>) Class.forName(simpleClassName);
                realTypeMapper.put(beanClass, realType);
            } catch (ClassNotFoundException e) {
            }
        }
        if (realType != null) {
            try {
                return realType.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("{create_bean_error}", e);
            }
        } else {
            throw new UnsupportedOperationException("{create_bean_error}:realType not found!");
        }
    }

    @Override
    public <T extends Bean> Class<T> getRealType(Class<T> beanClass) {
        return realTypeMapper.get(beanClass);
    }
}
