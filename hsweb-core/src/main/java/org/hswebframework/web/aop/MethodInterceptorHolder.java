/*
 *  Copyright 2019 http://www.hswebframework.org
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aopalliance.intercept.MethodInvocation;
import org.hswebframework.web.utils.AnnotationUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.DigestUtils;
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
        String id = DigestUtils.md5DigestAsHex(String.valueOf(invocation.getMethod().hashCode()).getBytes());
        String[] argNames = nameDiscoverer.getParameterNames(invocation.getMethod());
        Object[] args = invocation.getArguments();
        Map<String, Object> argMap = new LinkedHashMap<>();
        for (int i = 0, len = args.length; i < len; i++) {
            argMap.put((argNames == null || argNames[i] == null) ? "arg" + i : argNames[i], args[i]);
        }

        return new MethodInterceptorHolder(id,
                invocation.getMethod(),
                invocation.getThis(),
                args,
                argMap);
    }

    private String id;

    private Method method;

    private Object target;

    private Object[] arguments;

    private Map<String, Object> namedArguments;


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
                        args[i] = handler.apply(((Mono) arg));
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
