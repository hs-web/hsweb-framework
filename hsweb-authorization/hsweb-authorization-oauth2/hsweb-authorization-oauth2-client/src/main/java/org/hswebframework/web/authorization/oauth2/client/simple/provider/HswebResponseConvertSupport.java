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

package org.hswebframework.web.authorization.oauth2.client.simple.provider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.builder.AuthenticationBuilderFactory;
import org.hswebframework.web.authorization.oauth2.client.exception.OAuth2RequestException;
import org.hswebframework.web.authorization.oauth2.client.request.definition.ResponseConvertForProviderDefinition;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
@SuppressWarnings("unchecked")
public class HswebResponseConvertSupport implements ResponseConvertForProviderDefinition {

    private AuthenticationBuilderFactory authenticationBuilderFactory;

    private static int responseMessageFieldSize = 4;

    Function<Object, Authentication> autzParser = obj -> convertAuthentication(JSON.toJSONString(obj));

    private static final Set<String> springMvcErrorResponseKeys =
            new HashSet<>(Arrays.asList("exception", "path", "error", "message", "timestamp", "status"));

    public HswebResponseConvertSupport(AuthenticationBuilderFactory authenticationBuilderFactory) {
        this.authenticationBuilderFactory = authenticationBuilderFactory;
    }


    public Object tryConvertToObject(String json, Class type, OAuth2Response response) {
        if (json.startsWith("{")) {
            if (ResponseMessage.class.isAssignableFrom(type)) {
                return JSON.parseObject(json, type);
            }
            JSONObject message = JSON.parseObject(json, Feature.DisableFieldSmartMatch);
            //判断是否响应的为ResponseMessage
            if (message.size() <= responseMessageFieldSize
                    && message.get("status") != null && message.get("timestamp") != null) {

                Integer status = message.getInteger("status");
                if (status != 200) {
                    throw new BusinessException(message.getString("message"), status);
                }
                Object data = message.get("result");
                if (data == null) {
                    return null;
                }
                //返回的是对象
                if (data instanceof JSONObject) {
                    if (type == Authentication.class) {
                        return autzParser.apply(data);
                    }
                    return ((JSONObject) data).toJavaObject(type);
                }
                //返回的是集合
                if (data instanceof JSONArray) {
                    if (type == Authentication.class) {
                        return ((JSONArray) data).stream().map(autzParser).collect(Collectors.toList());
                    }
                    return ((JSONArray) data).toJavaList(type);
                }
                //return data;
                return message.getObject("result", type);
            }
            if (springMvcErrorResponseKeys.containsAll(message.keySet())) {
                throw new OAuth2RequestException(ErrorType.SERVICE_ERROR, response);
            }
            return message.toJavaObject(type);
        } else if (json.startsWith("[")) {
            if (type == Authentication.class) {
                return (JSON.parseArray(json)).stream().map(autzParser).collect(Collectors.toList());
            }
            return JSON.parseArray(json, type);
        }
        return null;
    }

    protected <T> T convertAuthentication(String json) {
        if (authenticationBuilderFactory != null) {
            return (T) authenticationBuilderFactory.create().json(json).build();
        } else {
            throw new UnsupportedOperationException("authenticationBuilderFactory not ready");
        }
    }

    @Override
    public <T> T convert(OAuth2Response response, Class<T> type) {
        String json = response.asString();

        Object data = tryConvertToObject(json, type, response);
        if (null == data) return null;
        if (type.isInstance(data)) {
            //success
            return ((T) data);
        }
        if (data instanceof ResponseMessage) {

            //maybe error
            throw new OAuth2RequestException(((ResponseMessage) data).getMessage(), ErrorType.SERVICE_ERROR, response);
        }

        throw new OAuth2RequestException(ErrorType.PARSE_RESPONSE_ERROR, response);
    }

    @Override
    @SuppressWarnings("all")
    public <T> List<T> convertList(OAuth2Response response, Class<T> type) {
        String json = response.asString();

        Object data = tryConvertToObject(json, type, response);
        if (null == data) return null;
        if (data instanceof List) {
            //success
            return ((List) data);
        }
        if (data instanceof ResponseMessage) {
            //maybe error
            throw new OAuth2RequestException(((ResponseMessage) data).getMessage(), ErrorType.SERVICE_ERROR, response);
        }

        throw new OAuth2RequestException(ErrorType.PARSE_RESPONSE_ERROR, response);
    }

    @Override
    public String getProvider() {
        return "hsweb";
    }
}
