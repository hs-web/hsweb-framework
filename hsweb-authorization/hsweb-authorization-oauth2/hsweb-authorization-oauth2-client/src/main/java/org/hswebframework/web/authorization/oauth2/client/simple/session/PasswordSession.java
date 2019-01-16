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

package org.hswebframework.web.authorization.oauth2.client.simple.session;

import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Request;
import org.hswebframework.web.oauth2.core.GrantType;
import org.hswebframework.web.oauth2.core.OAuth2Constants;

/**
 * @author zhouhao
 */
public class PasswordSession extends DefaultOAuth2Session {
    String username;
    String password;

    public PasswordSession(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected void applyBasicAuthParam(OAuth2Request request) {
        request.param(OAuth2Constants.grant_type, GrantType.password);
        request.param("username", username);
        request.param("password", serverConfig.getClientSecret());
        request.header(OAuth2Constants.authorization, encodeAuthorization(username.concat(":").concat(password)));
    }
}
