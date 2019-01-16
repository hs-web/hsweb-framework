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

package org.hswebframework.web.authorization.oauth2.server.support.client;

import org.hswebframework.web.authorization.oauth2.server.AuthorizationService;
import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;
import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;

/**
 * client_credential方式认证器
 *
 * @author zhouhao
 * @see org.hswebframework.web.oauth2.core.GrantType#client_credentials
 */
public interface ClientCredentialGranter extends AuthorizationService {
    /**
     * 申请token
     * @param request 请求参数
     * @return 申请成功的token信息
     * @throws GrantTokenException
     * @see org.hswebframework.web.oauth2.core.ErrorType
     */
    OAuth2AccessToken requestToken(ClientCredentialRequest request);
}
