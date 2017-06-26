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

package org.hswebframework.web.starter.oauth2.client;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.service.oauth2.client.request.definition.ResponseConvertForProviderDefinition;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Component
public class QQResponseConvertSupport implements ResponseConvertForProviderDefinition {
    @Override
    public <T> T convert(OAuth2Response response, Class<T> type) {
        String json = response.asString();
        if (json.contains("callback(")) {
            json = json.trim().substring("callback(".length(), json.length() - 3);
        }
        return JSON.parseObject(json, type);
    }

    @Override
    public <T> List<T> convertList(OAuth2Response response, Class<T> type) {
        String json = response.asString();
        if (json.contains("callback(")) {
            json = json.trim().substring("callback(".length(), json.length() - 3);
        }
        return JSON.parseArray(json, type);
    }

    @Override
    public String getProvider() {
        return "QQ";
    }
}
