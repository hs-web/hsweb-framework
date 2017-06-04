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

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.NotFoundException;
import org.hswebframwork.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author zhouhao
 * @since 3.0
 */
@SuppressWarnings("unchecked")
public class MapperEntityFactory implements EntityFactory {
    private Map<Class, Mapper>          realTypeMapper = new HashMap<>();
    private Logger                      logger         = LoggerFactory.getLogger(this.getClass());
    private Map<String, PropertyCopier> copierCache    = new HashMap<>();

    public MapperEntityFactory() {
    }

    public <T> MapperEntityFactory(Map<Class<T>, Mapper> realTypeMapper) {
        this.realTypeMapper.putAll(realTypeMapper);
    }

    public <T> MapperEntityFactory addMapping(Class<T> target, Mapper<T> mapper) {
        realTypeMapper.put(target, mapper);
        return this;
    }

    public MapperEntityFactory addCopier(PropertyCopier copier) {
        Class source = ClassUtils.getGenericType(copier.getClass(), 0);
        Class target = ClassUtils.getGenericType(copier.getClass(), 1);
        if (source == null || source == Object.class) {
            throw new UnsupportedOperationException("generic type " + source + " not support");
        }
        if (target == null || target == Object.class) {
            throw new UnsupportedOperationException("generic type " + target + " not support");
        }
        addCopier(source, target, copier);
        return this;
    }

    public <S, T> MapperEntityFactory addCopier(Class<S> source, Class<T> target, PropertyCopier<S, T> copier) {
        copierCache.put(getCopierCacheKey(source, target), copier);
        return this;
    }

    private String getCopierCacheKey(Class source, Class target) {
        return source.getName().concat("->").concat(target.getName());

    }

    @Override
    public <S, T> T copyProperties(S source, T target) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        try {
            PropertyCopier<S, T> copier = copierCache.<S, T>get(getCopierCacheKey(source.getClass(), target.getClass()));
            if (null != copier) return copier.copyProperties(source, target);

            return JSON.parseObject(JSON.toJSONString(source), (Class<T>) target.getClass());
        } catch (Exception e) {
            logger.warn("copy properties error", e);
        }
        return target;
    }

    protected <T> Mapper<T> initCache(Class<T> beanClass) {
        Mapper<T> mapper = null;
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
        }
        return mapper;
    }

    @Override
    public <T> T newInstance(Class<T> beanClass) {
        if (beanClass == null) return null;
        Mapper<T> mapper = realTypeMapper.get(beanClass);
        if (mapper != null) return mapper.getInstanceGetter().get();
        mapper = initCache(beanClass);
        if (mapper != null) return mapper.getInstanceGetter().get();

        throw new NotFoundException("can't create instance for " + beanClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<T> getInstanceType(Class<T> beanClass) {
        Mapper<T> mapper = realTypeMapper.get(beanClass);
        if (null != mapper) {
            return mapper.getTarget();
        }
        mapper = initCache(beanClass);
        if (mapper != null)
            return mapper.getTarget();
        return beanClass;
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
