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

package org.hswebframework.web.service.oauth2.client.simple.provider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.builder.AuthenticationBuilderFactory;
import org.hswebframework.web.authorization.oauth2.client.AccessTokenInfo;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.service.oauth2.client.request.ProviderSupport;
import org.hswebframework.web.service.oauth2.client.request.definition.ResponseConvertForProviderDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Component
public class HswebResponseConvertSupport implements ResponseConvertForProviderDefinition {

    private AuthenticationBuilderFactory authenticationBuilderFactory;

    @Autowired(required = false)
    public void setAuthenticationBuilderFactory(AuthenticationBuilderFactory authenticationBuilderFactory) {
        this.authenticationBuilderFactory = authenticationBuilderFactory;
    }

    @Override
    public <T> T convert(OAuth2Response response, Class<T> type) {
        String json = response.asString();
        if (type == Authentication.class) {
            if (authenticationBuilderFactory != null) {
                return (T) authenticationBuilderFactory.create().json(json).build();
            } else {
                throw new UnsupportedOperationException("authenticationBuilderFactory not ready");
            }
        }
        return JSON.parseObject(json, type);
    }

    @Override
    public <T> List<T> convertList(OAuth2Response response, Class<T> type) {
        String json = response.asString();
        return JSON.parseArray(json, type);
    }

    @Override
    public String getProvider() {
        return ProviderSupport.hsweb;
    }
}
