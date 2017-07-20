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

package org.hswebframework.web.authorization.oauth2.client;

import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Session;

/**
 * OAuth2会话创建器,根据各种方式创建 OAuth2会话
 *
 * @author zhouhao
 * @see OAuth2Session
 * @since 3.0
 */
public interface OAuth2SessionBuilder {

    /**
     * 根据授权码方式创建会话
     *
     * @param code 授权码
     * @return 会话
     * @see "grant_type=authorization_code"
     */
    OAuth2Session byAuthorizationCode(String code);

    /**
     * 根据密钥方式创建会话
     *
     * @return 会话
     * @see "grant_type=client_credentials"
     */
    OAuth2Session byClientCredentials();

    /**
     * 根据密码方式创建会话
     *
     * @return 会话
     * @see "grant_type=password"
     */
    OAuth2Session byPassword(String username, String password);

    /**
     * 直接指定accessToken创建会话
     *
     * @param accessToken
     * @return 会话
     */
    OAuth2Session byAccessToken(String accessToken);

}
