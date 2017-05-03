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
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.hswebframework.web.service.oauth2.client.request.ProviderSupport;
import org.hswebframework.web.service.oauth2.client.request.definition.ResponseJudgeForProviderDefinition;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Component
public class HswebResponseJudgeSupport implements ResponseJudgeForProviderDefinition {

    @Override
    public String getProvider() {
        return ProviderSupport.hsweb;
    }

    @Override
    public ErrorType judge(OAuth2Response response) {
        String result = response.asString();
        if (result == null) return ErrorType.OTHER;
        JSONObject jsonRes = JSON.parseObject(result);
        Integer status = jsonRes.getInteger("status");
        if (status == null && response.status() == 200) return null;
        if (status != null) {
            if (status == 200) return null;
            return ErrorType.fromCode(status).orElse(ErrorType.OTHER);
        }
        if (jsonRes.get("message") != null) {
            return ErrorType.valueOf(jsonRes.getString("message"));
        }
        return null;
    }
}
