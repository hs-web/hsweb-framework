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

package org.hswebframework.web.service.oauth2.client.starter;

import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.service.oauth2.client.request.ResponseJudge;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DefaultResponseJudge implements ResponseJudge {
    private static List<OAuth2Response.ErrorType> errorTypes = Arrays.stream(OAuth2Response.ErrorType.values())
            .filter(errorType -> errorType != OAuth2Response.ErrorType.OTHER)
            .collect(Collectors.toList());

    @Override
    public OAuth2Response.ErrorType judge(OAuth2Response response) {
        if (response.status() == 200) return null;
        String result = response.asString();
        if (result == null) return OAuth2Response.ErrorType.OTHER;
        return errorTypes.stream()
                .filter(errorType -> result.contains(errorType.name().toLowerCase()))
                .findAny().orElse(null);
    }
}
