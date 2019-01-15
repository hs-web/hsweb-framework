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

package org.hswebframework.web.authorization.oauth2.client.request;

import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.oauth2.core.ErrorType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author zhouhao
 */
public class DefaultResponseJudge implements ResponseJudge {
    private static List<ErrorType> errorTypes = Arrays.stream(ErrorType.values())
            .filter(errorType -> errorType != ErrorType.OTHER)
            .collect(Collectors.toList());

    @Override
    public ErrorType judge(OAuth2Response response) {
        if (response.status() == 200) {
            return null;
        }
        String result = response.asString();
        if (result == null) {
            return ErrorType.OTHER;
        }
        return errorTypes.stream()
                .filter(errorType -> result.contains(errorType.name().toLowerCase()))
                .findAny().orElse(null);
    }
}
