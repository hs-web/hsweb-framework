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

import org.apache.commons.beanutils.BeanUtils;
import org.hswebframework.web.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author zhouhao
 * @since 3.0
 */
public class MapperEntityFactory implements EntityFactory {
    private Map<Class, Mapper> realTypeMapper = new HashMap<>();
    private Logger             logger         = LoggerFactory.getLogger(this.getClass());

    public MapperEntityFactory() {
    }

    public <T> MapperEntityFactory(Map<Class<T>, Mapper> realTypeMapper) {
        this.realTypeMapper.putAll(realTypeMapper);
    }

    public <T> MapperEntityFactory addMapping(Class<T> target, Mapper<T> mapper) {
        realTypeMapper.put(target, mapper);
        return this;
    }

    @Override
    public <S, T> T copyProperties(S source, T target) {
        try {
            // TODO: 17-3-30 应该设计为可自定义
            BeanUtils.copyProperties(target, source);
        } catch (Exception e) {
            logger.warn("copy properties error", e);
        }
        return target;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T newInstance(Class<T> beanClass) {
        if (beanClass == null) return null;
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
                } catch (ClassNotFoundException e) {
                    throw new NotFoundException(e.getMessage());
                }
            }
            if (realType != null) {
                mapper = new Mapper<>(realType, new DefaultInstanceGetter(realType));
                realTypeMapper.put(beanClass, mapper);
                return mapper.getInstanceGetter().get();
            }
        }
        throw new NotFoundException("can't create instance for " + beanClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<T> getInstanceType(Class<T> beanClass) {
        Mapper<T> mapper = realTypeMapper.get(beanClass);
        if (null != mapper) {
            return mapper.getTarget();
        }
        return null;
    }

    public static class Mapper<T> {
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

    public static <T> Mapper<T> defaultMapper(Class<T> target) {
        return new Mapper<>(target, defaultInstanceGetter(target));
    }

    public static <T> Supplier<T> defaultInstanceGetter(Class<T> clazz) {
        return new DefaultInstanceGetter<>(clazz);
    }

    static class DefaultInstanceGetter<T> implements Supplier<T> {
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
