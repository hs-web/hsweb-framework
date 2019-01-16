/*
 * Copyright 2019 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.starter.resolver;

import org.hswebframework.web.starter.convert.FastJsonGenericHttpMessageConverter;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class JsonParamResolver implements HandlerMethodArgumentResolver {

    private FastJsonGenericHttpMessageConverter fastJsonHttpMessageConverter;

    public JsonParamResolver(FastJsonGenericHttpMessageConverter fastJsonHttpMessageConverter) {
        this.fastJsonHttpMessageConverter = fastJsonHttpMessageConverter;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(JsonParam.class) && fastJsonHttpMessageConverter != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        JsonParam jsonParam = parameter.getParameterAnnotation(JsonParam.class);
        String object = webRequest.getParameter(jsonParam.value());
        if (null != object) {
            Class type = jsonParam.type() != Void.class ? jsonParam.type() : parameter.getParameterType();
            return fastJsonHttpMessageConverter.readByString(type, object);
        }
        return null;
    }

}
