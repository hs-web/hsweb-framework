/*
 *  Copyright 2020 http://www.hswebframework.org
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

package org.hswebframework.web.aop;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.aopalliance.intercept.MethodInvocation;
import org.hswebframework.web.utils.AnnotationUtils;
import org.hswebframework.web.utils.DigestUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author zhouhao
 */
@AllArgsConstructor
@Getter
public class MethodInterceptorHolder {
    /**
     * 参数名称获取器,用于获取方法参数的名称
     */
    public static final ParameterNameDiscoverer nameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    public static MethodInterceptorHolder create(MethodInvocation invocation) {
        String[] argNames = nameDiscoverer.getParameterNames(invocation.getMethod());
        Object[] args = invocation.getArguments();

        String[] names;
        //参数名与参数长度不一致，则填充argx来作为参数名
        if (argNames == null || argNames.length != args.length) {
            names = new String[args.length];
            for (int i = 0, len = args.length; i < len; i++) {
                names[i] = (argNames == null || argNames.length <= i || argNames[i] == null) ? "arg" + i : argNames[i];
            }
        } else {
            names = argNames;
        }
        return new MethodInterceptorHolder(null,
                                           invocation.getMethod(),
                                           invocation.getThis(),
                                           args,
                                           names,
                                           null);
    }

    private String id;

    private final Method method;

    private final Object target;

    private final Object[] arguments;

    private final String[] argumentsNames;

    private Map<String, Object> namedArguments;

    public String getId() {
        if (id == null) {
            id = DigestUtils.md5Hex(method.toString());
        }
        return id;
    }

    protected Map<String, Object> createNamedArguments() {
        Map<String, Object> namedArguments = Maps.newLinkedHashMapWithExpectedSize(arguments.length);
        for (int i = 0, len = arguments.length; i < len; i++) {
            namedArguments.put(argumentsNames[i], arguments[i]);
        }
        return namedArguments;

    }

    public Map<String, Object> getNamedArguments() {
        return namedArguments == null ? namedArguments = createNamedArguments() : namedArguments;
    }

    public <T extends Annotation> T findMethodAnnotation(Class<T> annClass) {
        return AnnotationUtils.findMethodAnnotation(annClass, method, annClass);
    }

    public <T extends Annotation> T findClassAnnotation(Class<T> annClass) {
        return AnnotationUtils.findAnnotation(target.getClass(), annClass);
    }

    public <T extends Annotation> T findAnnotation(Class<T> annClass) {
        return AnnotationUtils.findAnnotation(target.getClass(), method, annClass);
    }

    public MethodInterceptorContext createParamContext() {
        return createParamContext(null);
    }

    public MethodInterceptorContext createParamContext(Object invokeResult) {
        return new MethodInterceptorContext() {
            private static final long serialVersionUID = -4102787561601219273L;
            private Object result = invokeResult;

            @Override
            public Object[] getArguments() {
                return arguments;
            }

            public boolean handleReactiveArguments(Function<Publisher<?>, Publisher<?>> handler) {
                boolean handled = false;
                Object[] args = getArguments();
                if (args == null || args.length == 0) {
                    return false;
                }
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Publisher) {
                        args[i] = handler.apply(((Publisher<?>) arg));
                        handled = true;
                    }
                }

                return handled;
            }


            @Override
            public Object getTarget() {
                return target;
            }

            @Override
            public Method getMethod() {
                return method;
            }

            @Override
            public <T> Optional<T> getArgument(String name) {
                if (namedArguments == null) {
                    return Optional.empty();
                }
                return Optional.ofNullable((T) namedArguments.get(name));
            }

            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annClass) {
                return findAnnotation(annClass);
            }

            @Override
            public Map<String, Object> getNamedArguments() {
                return MethodInterceptorHolder.this.getNamedArguments();
            }

            @Override
            public Object getInvokeResult() {
                return result;
            }

            @Override
            public void setInvokeResult(Object result) {
                this.result = result;
            }
        };
    }
}
