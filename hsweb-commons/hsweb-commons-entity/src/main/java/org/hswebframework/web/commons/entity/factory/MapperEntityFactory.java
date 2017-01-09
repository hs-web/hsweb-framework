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

import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.commons.entity.Entity;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since 3.0
 */
public class MapperEntityFactory implements EntityFactory {
    private Map<Class, Mapper> realTypeMapper = new HashMap<>();

    public <T extends Entity> MapperEntityFactory addMapping(Class<T> target, Mapper<T> mapper) {
        realTypeMapper.put(target, mapper);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> T newInstance(Class<T> beanClass) {
        Mapper<T> mapper = realTypeMapper.get(beanClass);
        if (mapper != null) return mapper.getInstanceGetter().get();
        synchronized (beanClass) {
            mapper = realTypeMapper.get(beanClass);
            if (mapper != null) return mapper.getInstanceGetter().get();
            Class<T> realType = null;
            if (!Modifier.isInterface(beanClass.getModifiers()) && !Modifier.isAbstract(beanClass.getModifiers())) {
                realType = beanClass;
            }
            //尝试使用 Simple类，如: package.SimpleUserBean
            if (realType == null) {
                String simpleClassName = beanClass.getPackage().getName().concat(".Simple").concat(beanClass.getSimpleName());
                try {
                    realType = (Class<T>) Class.forName(simpleClassName);
                    mapper = new Mapper<>(realType, new DefaultInstanceGetter(realType));
                    realTypeMapper.put(beanClass, mapper);
                    return mapper.getInstanceGetter().get();
                } catch (ClassNotFoundException e) {
                    throw new NotFoundException(e.getMessage());
                }
            }
        }
        throw new NotFoundException("can't create instance for " + beanClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> Class<T> getInstanceType(Class<T> beanClass) {
        Mapper<T> mapper = realTypeMapper.get(beanClass);
        if (null != mapper) {
            return mapper.getTarget();
        }
        return null;
    }

    public static class Mapper<T extends Entity> {
        Class<T>    target;
        Supplier<T> instanceGetter;

        public Mapper(Class<T> target, Supplier<T> instanceGetter) {
            this.target = target;
            this.instanceGetter = instanceGetter;
        }

        public Class<T> getTarget() {
            return target;
        }

        public Supplier<T> getInstanceGetter() {
            return instanceGetter;
        }
    }

    class DefaultInstanceGetter<T extends Entity> implements Supplier<T> {
        Class<T> type;

        public DefaultInstanceGetter(Class<T> type) {
            this.type = type;
        }

        @Override
        public T get() {
            try {
                return type.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
