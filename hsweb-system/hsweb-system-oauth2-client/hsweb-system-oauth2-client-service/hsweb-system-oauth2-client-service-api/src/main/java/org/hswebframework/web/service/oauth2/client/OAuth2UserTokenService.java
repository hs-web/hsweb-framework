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
 */
package org.hswebframework.web.service.oauth2.client;

import org.hswebframework.web.entity.oauth2.client.OAuth2UserTokenEntity;
import org.hswebframework.web.service.CrudService;

import java.util.List;

/**
 * OAuth2用户授权信息 服务类
 *
 * @author hsweb-generator-online
 */
public interface OAuth2UserTokenService extends CrudService<OAuth2UserTokenEntity, String> {
    List<OAuth2UserTokenEntity> selectByServerIdAndGrantType(String serverId, String grantType);

    OAuth2UserTokenEntity selectByAccessToken(String accessToken);
}
