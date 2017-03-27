/*
 *  Copyright 2016 http://www.hswebframework.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.hswebframework.web.boost.aop.context;

import org.hswebframework.web.AopUtils;
import org.hswebframework.web.ThreadLocalUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author zhouhao
 */
public class MethodInterceptorHolder {

    public static MethodInterceptorHolder current() {
        return ThreadLocalUtils.get(MethodInterceptorHolder.class.getName());
    }

    public static MethodInterceptorHolder clear() {
        return ThreadLocalUtils.getAndRemove(MethodInterceptorHolder.class.getName());
    }

    public static MethodInterceptorHolder setCurrent(MethodInterceptorHolder holder) {
        return ThreadLocalUtils.put(MethodInterceptorHolder.class.getName(), holder);
    }

    private String id;

    private Method method;

    private Object target;

    private Map<String, Object> args;

    public void set() {
        MethodInterceptorHolder.setCurrent(this);
    }

    public MethodInterceptorHolder(String id, Method method, Object target, Map<String, Object> args) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(id);
        Objects.requireNonNull(method);
        Objects.requireNonNull(target);
        Objects.requireNonNull(args);
        this.id = id;
        this.method = method;
        this.target = target;
        this.args = args;
    }

    public String getId() {
        return id;
    }

    public Method getMethod() {
        return method;
    }

    public Object getTarget() {
        return target;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public MethodInterceptorParamContext createParamContext() {
        return new MethodInterceptorParamContext() {
            @Override
            public Object getTarget() {
                return target;
            }

            @Override
            public Method getMethod() {
                return method;
            }

            @Override
            public <T> Optional<T> getParameter(String name) {
                if (args == null) return Optional.empty();
                return Optional.of((T) args.get(name));
            }

            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annClass) {
                return AopUtils.findAnnotation(target.getClass(), method, annClass);
            }

            @Override
            public Map<String, Object> getParams() {
                return getArgs();
            }
        };
    }
}
