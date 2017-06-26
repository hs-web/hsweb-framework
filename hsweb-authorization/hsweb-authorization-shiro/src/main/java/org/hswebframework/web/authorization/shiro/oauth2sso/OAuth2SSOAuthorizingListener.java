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

package org.hswebframework.web.authorization.shiro.oauth2sso;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2CodeAuthBeforeEvent;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2Listener;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.authorization.shiro.SimpleAuthenticationToken;

/**
 * @author zhouhao
 */
public class OAuth2SSOAuthorizingListener
        implements OAuth2Listener<OAuth2CodeAuthBeforeEvent> {

    private OAuth2RequestService oAuth2RequestService;

    private String userCenterServerId;

    private String userAuthInfoApi = "oauth2/user-auth-info";

    public OAuth2SSOAuthorizingListener(OAuth2RequestService oAuth2RequestService, String userCenterServerId) {
        this.oAuth2RequestService = oAuth2RequestService;
        this.userCenterServerId = userCenterServerId;
    }

    public void setUserAuthInfoApi(String userAuthInfoApi) {
        this.userAuthInfoApi = userAuthInfoApi;
    }

    public void setUserCenterServerId(String userCenterServerId) {
        this.userCenterServerId = userCenterServerId;
    }

    @Override
    public void on(OAuth2CodeAuthBeforeEvent event) {
        Authentication authentication = oAuth2RequestService
                .create(userCenterServerId)
                .byAuthorizationCode(event.getCode())
                .request(userAuthInfoApi)
                .get().onError(OAuth2Response.throwOnError)
                .as(Authentication.class);

        boolean remember = Boolean.valueOf(event.getParameter("remember").orElse("false"));
        Subject subject = SecurityUtils.getSubject();
        subject.login(new SimpleAuthenticationToken(authentication, remember));

    }
}
