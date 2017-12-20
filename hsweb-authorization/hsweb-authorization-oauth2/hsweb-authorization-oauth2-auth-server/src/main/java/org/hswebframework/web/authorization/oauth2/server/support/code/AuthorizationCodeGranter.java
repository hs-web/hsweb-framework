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

import org.hswebframework.web.authorization.oauth2.server.AuthorizationService;
import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;

/**
 * authorization_code方式申请token
 *
 * @author zhouhao
 * @see org.hswebframework.web.oauth2.core.GrantType#authorization_code
 */
public interface AuthorizationCodeGranter extends AuthorizationService {
    /**
     * 申请token
     * @param request
     * @return
     */
    OAuth2AccessToken requestToken(AuthorizationCodeTokenRequest request);
}
