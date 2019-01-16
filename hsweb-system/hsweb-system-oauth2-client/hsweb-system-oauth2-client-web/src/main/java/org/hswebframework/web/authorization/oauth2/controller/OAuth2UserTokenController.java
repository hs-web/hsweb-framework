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

package org.hswebframework.web.authorization.oauth2.controller;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.GenericEntityController;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.entity.oauth2.client.OAuth2UserTokenEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.oauth2.client.OAuth2UserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth2用户授权信息
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.oauth2-user-token:oauth2-user-token}")
@Authorize(permission = "oauth2-user-token",description = "OAuth2.0-客户端-token管理")
@Api(tags = "OAuth2.0-客户端-token",value = "OAuth2.0-客户端-token")
public class OAuth2UserTokenController
        implements QueryController<OAuth2UserTokenEntity, String, QueryParamEntity> {

    private OAuth2UserTokenService oAuth2UserTokenService;

    @Autowired
    public void setOAuth2UserTokenService(OAuth2UserTokenService oAuth2UserTokenService) {
        this.oAuth2UserTokenService = oAuth2UserTokenService;
    }

    @Override
    public OAuth2UserTokenService getService() {
        return oAuth2UserTokenService;
    }
}
