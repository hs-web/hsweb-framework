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

package org.hswebframework.web.authorization.oauth2.api.entity;

import org.hswebframework.web.commons.entity.Entity;

/**
 * @author zhouhao
 */
public interface OAuth2AccessEntity extends Entity {

    String getClientId();

    void setClientId(String clientId);

    String getUserId();

    void setUserId(String userId);

    String getAccessToken();

    void setAccessToken(String accessToken);

    String getRefreshToken();

    void setRefreshToken(String refreshToken);

    Long getExpiresIn();

    void setExpiresIn(Long expiresIn);

    Long getCreateTime();

    void setCreateTime(Long createTime);

    Long getUpdateTime();

    void setUpdateTime(Long updateTime);

    String getScope();

    void setScope(String scope);


}
