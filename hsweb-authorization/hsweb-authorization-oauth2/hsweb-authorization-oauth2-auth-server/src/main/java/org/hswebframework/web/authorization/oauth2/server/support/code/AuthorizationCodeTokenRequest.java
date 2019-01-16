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

package org.hswebframework.web.authorization.oauth2.server.support.code;

import org.hswebframework.web.authorization.oauth2.server.TokenRequest;

import java.util.Set;

/**
 * 授权码方式token请求
 *
 * @author zhouhao
 */
public interface AuthorizationCodeTokenRequest extends TokenRequest {
    /**
     * @return 搜权码
     */
    String getCode();

    /**
     * @return oauth2客户端id
     */
    String getClientId();

    /**
     * @return oauth2客户端密钥
     */
    String getClientSecret();

    /**
     * @return 申请授权范围
     */
    Set<String> getScope();

    /**
     * @return 重定向地址
     */
    String getRedirectUri();
}
