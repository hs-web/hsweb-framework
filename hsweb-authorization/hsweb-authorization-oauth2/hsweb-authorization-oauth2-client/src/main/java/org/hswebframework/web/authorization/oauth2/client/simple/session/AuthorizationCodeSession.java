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

package org.hswebframework.web.authorization.oauth2.client.simple.session;

import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Request;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Session;
import org.hswebframework.web.oauth2.core.GrantType;
import org.hswebframework.web.oauth2.core.OAuth2Constants;

/**
 * @author zhouhao
 */
public class AuthorizationCodeSession extends DefaultOAuth2Session {
    private String code;

    private boolean init = false;

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    protected void applyBasicAuthParam(OAuth2Request request) {
        super.applyBasicAuthParam(request);
        request.param(OAuth2Constants.grant_type, GrantType.authorization_code);
    }

    @Override
    public OAuth2Session authorize() {
        if (init) {
            throw new UnsupportedOperationException("AuthorizationCode模式不能重复连接");
        }
        accessTokenRequest.param("code", code);
        super.authorize();
        init = true;
        return this;
    }
}
