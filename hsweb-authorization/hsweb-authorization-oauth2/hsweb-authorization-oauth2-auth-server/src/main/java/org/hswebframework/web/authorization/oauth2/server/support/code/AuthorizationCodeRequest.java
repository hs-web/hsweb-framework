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

package org.hswebframework.web.authorization.oauth2.server.support.code;

import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.oauth2.server.client.OAuth2Client;

import java.util.Set;

/**
 * 授权码请求
 *
 * @author zhouhao
 */
public interface AuthorizationCodeRequest {
    /**
     * @return oauth2客户端id
     * @see org.hswebframework.web.oauth2.core.OAuth2Constants#client_id
     * @see OAuth2Client#getId()
     */
    String getClientId();

    /**
     * @return 与授权码关联的用户ID
     * @see User#getId()
     */
    String getUserId();

    /**
     * @return 允许授权的范围
     */
    Set<String> getScope();

    /**
     * @return 重定向地址
     * @see org.hswebframework.web.oauth2.core.OAuth2Constants#redirect_uri
     */
    String getRedirectUri();
}
