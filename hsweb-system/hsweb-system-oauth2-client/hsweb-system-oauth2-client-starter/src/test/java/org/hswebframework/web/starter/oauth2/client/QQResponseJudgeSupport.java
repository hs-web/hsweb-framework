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
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.authorization.oauth2.client.request.definition.ResponseJudgeForProviderDefinition;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Component
public class QQResponseJudgeSupport implements ResponseJudgeForProviderDefinition {
    static Map<String, ErrorType> errorTypeMap = new HashMap<>();

    static {
        /*
        http://wiki.connect.qq.com/%E5%85%AC%E5%85%B1%E8%BF%94%E5%9B%9E%E7%A0%81%E8%AF%B4%E6%98%8E
         */
        // success
        errorTypeMap.put("0", null);

        errorTypeMap.put("100000", ErrorType.ILLEGAL_RESPONSE_TYPE);
        errorTypeMap.put("100001", ErrorType.ILLEGAL_CLIENT_ID);
        // missing
        errorTypeMap.put("100002", ErrorType.ILLEGAL_CLIENT_SECRET);
        errorTypeMap.put("100003", ErrorType.ILLEGAL_AUTHORIZATION);
        errorTypeMap.put("100004", ErrorType.ILLEGAL_GRANT_TYPE);
        errorTypeMap.put("100005", ErrorType.ILLEGAL_CODE);
        errorTypeMap.put("100006", ErrorType.ILLEGAL_REFRESH_TOKEN);
        errorTypeMap.put("100007", ErrorType.ILLEGAL_ACCESS_TOKEN);
        //param error
        errorTypeMap.put("100009", ErrorType.ILLEGAL_CLIENT_SECRET);
        errorTypeMap.put("100010", ErrorType.ILLEGAL_REDIRECT_URI);
        errorTypeMap.put("100013", ErrorType.ILLEGAL_ACCESS_TOKEN);
        errorTypeMap.put("100014", ErrorType.EXPIRED_TOKEN);
        errorTypeMap.put("100015", ErrorType.INVALID_TOKEN);

        errorTypeMap.put("100016", ErrorType.ILLEGAL_ACCESS_TOKEN);

        errorTypeMap.put("100019", ErrorType.ILLEGAL_CODE);

    }

    @Override
    public String getProvider() {
        return "QQ";
    }

    @Override
    public ErrorType judge(OAuth2Response response) {
        String result = response.asString();
        if (result == null) return ErrorType.OTHER;
        if (result.contains("callback(")) {
            result = result.substring("callback(".length(), result.length() - 3);
        }
        JSONObject jsonRes = JSON.parseObject(result);
        String error = jsonRes.getString("error");
        if (error != null) {
            return errorTypeMap.get(error);
        }
        return null;
    }
}
