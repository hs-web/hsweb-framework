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

package org.hswebframework.web.example.oauth2;

import org.hswebframework.web.WebUtil;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2CodeAuthBeforeEvent;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2Listener;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Session;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.authorization.token.UserTokenManager;

import javax.servlet.http.HttpSession;

/**
 * @author zhouhao
 */
public class OAuth2SSOAuthorizingListener
        implements OAuth2Listener<OAuth2CodeAuthBeforeEvent> {

    private OAuth2RequestService oAuth2RequestService;

    private UserTokenManager userTokenManager;


    private String userCenterServerId;

    private String userAuthInfoApi = "oauth2/user-auth-info";

    public OAuth2SSOAuthorizingListener(OAuth2RequestService oAuth2RequestService, String userCenterServerId, UserTokenManager userTokenManager) {
        this.oAuth2RequestService = oAuth2RequestService;
        this.userCenterServerId = userCenterServerId;
        this.userTokenManager = userTokenManager;
    }

    public void setUserAuthInfoApi(String userAuthInfoApi) {
        this.userAuthInfoApi = userAuthInfoApi;
    }

    public void setUserCenterServerId(String userCenterServerId) {
        this.userCenterServerId = userCenterServerId;
    }

    @Override
    public void on(OAuth2CodeAuthBeforeEvent event) {
        OAuth2Session session = oAuth2RequestService
                .create(userCenterServerId)
                .byAuthorizationCode(event.getCode())
                .authorize();

        Authentication authentication = session
                .request(userAuthInfoApi)
                .get().onError(OAuth2Response.throwOnError)
                .as(Authentication.class);

        HttpSession httpSession = WebUtil
                .getHttpServletRequest()
                .getSession();

        userTokenManager.signIn(httpSession.getId(), authentication.getUser().getId(), 60 * 60 * 1000);


    }
}
