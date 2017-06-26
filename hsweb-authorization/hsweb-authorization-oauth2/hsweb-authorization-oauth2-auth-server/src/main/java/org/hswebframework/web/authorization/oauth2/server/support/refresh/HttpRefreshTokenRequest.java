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

package org.hswebframework.web.authorization.oauth2.server.support.refresh;

import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.authorization.oauth2.server.support.HttpTokenRequest;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.hswebframework.web.oauth2.core.OAuth2Constants;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class HttpRefreshTokenRequest extends HttpTokenRequest implements RefreshTokenRequest {

    public HttpRefreshTokenRequest(HttpServletRequest request) {
        super(request);
        if (clientCredentials == null) {
            ErrorType.OTHER.throwThis(GrantTokenException::new,
                    "missing parameter:"
                            + OAuth2Constants.client_id + ","
                            + OAuth2Constants.client_secret + ","
                            + OAuth2Constants.authorization);
        }
    }

    @Override
    public String getClientId() {
        return clientCredentials.getPrincipal();
    }

    @Override
    public String getClientSecret() {
        return clientCredentials.getCredentials();
    }

    @Override
    public String getRefreshToken() {
        return getParameter(OAuth2Constants.refresh_token).orElse(null);
    }

    @Override
    public Set<String> getScope() {
        return scope;
    }
}
