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

package org.hswebframework.web.authorization.oauth2.server.support;

import org.hswebframework.web.WebUtil;
import org.hswebframework.web.authorization.oauth2.server.TokenRequest;
import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.hswebframework.web.oauth2.core.OAuth2Constants;
import org.hswebframework.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author zhouhao
 */
public class HttpTokenRequest implements TokenRequest {

    protected Map<String, String> parameters;
    protected Map<String, String> headers;
    protected Set<String>         scope;

    protected ClientCredentials clientCredentials;

    public HttpTokenRequest(HttpServletRequest request) {
        this.parameters = WebUtil.getParameters(request);
        this.headers = WebUtil.getHeaders(request);
        String clientId = parameters.get(OAuth2Constants.client_id);
        String clientSecret = parameters.get(OAuth2Constants.client_secret);
        String authorization = headers.get(OAuth2Constants.authorization);
        clientCredentials = getClientCredentials(clientId, clientSecret, authorization);

        this.scope = getParameter(OAuth2Constants.scope)
                .filter(scopeStr -> !StringUtils.isNullOrEmpty(scopeStr))
                .map(scopeStr -> new HashSet<>(Arrays.asList(scopeStr.split("[, \n]"))))
                .orElseGet(HashSet::new);
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    protected class ClientCredentials {
        private String principal;
        private String credentials;

        public ClientCredentials(String principal, String credentials) {
            this.principal = principal;
            this.credentials = credentials;
        }

        public String getPrincipal() {
            return principal;
        }

        public String getCredentials() {
            return credentials;
        }
    }

    protected ClientCredentials getClientCredentials(String principal, String credentials, String authorization) {
        if ((principal == null || credentials == null) && authorization == null) {
            return null;
        }
        if (authorization != null && !authorization.isEmpty()) {
            String[] decodeCredentials = decodeClientAuthenticationHeader(authorization);
            //fix #63
            if (decodeCredentials == null) {
                return null;
            }
            if (decodeCredentials.length > 1) {
                principal = decodeCredentials[0];
                credentials = decodeCredentials[1];
            } else {
                credentials = decodeCredentials[0];
            }
        }
        return new ClientCredentials(principal, credentials);
    }


    protected String[] decodeClientAuthenticationHeader(String authenticationHeader) {
        if (StringUtils.isNullOrEmpty(authenticationHeader)) {
            return null;
        } else {
            String[] tokens = authenticationHeader.split(" ");
            if (tokens.length != 2) {
                return null;
            } else {
                String authType = tokens[0];
                if (!"basic".equalsIgnoreCase(authType)) {
                    return ErrorType.OTHER.throwThis(GrantTokenException::new, "authentication " + authType + " not support!");
                } else {
                    String encodedCreds = tokens[1];
                    return decodeBase64EncodedCredentials(encodedCreds);
                }
            }
        }
    }

    protected String[] decodeBase64EncodedCredentials(String encodedCredentials) {
        String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials));
        String[] credentials = decodedCredentials.split(":", 2);
        return credentials.length != 2 ? null : (!StringUtils.isNullOrEmpty(credentials[0]) && !StringUtils.isNullOrEmpty(credentials[1]) ? credentials : null);
    }
}
