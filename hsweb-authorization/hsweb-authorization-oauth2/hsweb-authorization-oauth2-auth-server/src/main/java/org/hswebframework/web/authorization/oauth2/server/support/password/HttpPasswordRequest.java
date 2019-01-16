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

package org.hswebframework.web.authorization.oauth2.server.support.password;

import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.authorization.oauth2.server.support.HttpTokenRequest;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.hswebframework.web.oauth2.core.OAuth2Constants;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @author zhouhao
 */
public class HttpPasswordRequest extends HttpTokenRequest implements PasswordRequest {
    public HttpPasswordRequest(HttpServletRequest request) {
        super(request);
        clientCredentials = getClientCredentials(
                parameters.get(OAuth2Constants.username),
                parameters.get(OAuth2Constants.password),
                headers.get(OAuth2Constants.authorization));
        if (clientCredentials == null) {
            ErrorType.OTHER.throwThis(GrantTokenException::new, "missing parameter:" + OAuth2Constants.username + "," + OAuth2Constants.password + "," + OAuth2Constants.authorization);
        }
    }

    @Override
    public String getUsername() {
        return clientCredentials.getPrincipal();
    }

    @Override
    public String getPassword() {
        return clientCredentials.getCredentials();
    }

    @Override
    public Set<String> getScope() {
        return scope;
    }
}
