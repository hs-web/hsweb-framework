/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.oauth2.simple;

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.authorize.oauth2.OAuth2Manager;
import org.hsweb.web.oauth2.service.OAuth2Service;

import javax.servlet.http.HttpServletRequest;

public class SimpleOAuth2Manager implements OAuth2Manager {

    private OAuth2Service oAuth2Service;

    public SimpleOAuth2Manager(OAuth2Service oAuth2Service) {
        this.oAuth2Service = oAuth2Service;
    }

    @Override
    public String getAccessTokenByRequest(HttpServletRequest request) {
        String token = request.getHeader("access_token");
        if (token == null) {
            String authorization = request.getHeader("Authorization");
            if (authorization != null) {
                String[] arr = authorization.split("[ ]");
                if (arr.length == 2) token = arr[1];
            }
        }
        if (token == null)
            token = request.getParameter("access_token");
        return token;
    }

    @Override
    public User getUserByAccessToken(String accessToken) {
        return oAuth2Service.getUserByAccessToken(accessToken);
    }
}
