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

package org.hswebframework.web.authorization.oauth2.server;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

/**
 * @author zhouhao
 */
public interface OAuth2AccessToken extends Serializable {

    @NotBlank
    String getClientId();

    void setClientId(String clientId);

    @NotBlank
    String getAccessToken();

    void setAccessToken(String accessToken);

    @NotBlank
    String getRefreshToken();

    void setRefreshToken(String refreshToken);

    @NotNull
    Long getCreateTime();

    void setCreateTime(Long createTime);

    Long getUpdateTime();

    void setUpdateTime(Long updateTime);

    @NotNull
    String getOwnerId();

    void setOwnerId(String ownerId);

    @NotNull
    Integer getExpiresIn();

    void setExpiresIn(Integer expiresIn);

    Set<String> getScope();

    void setScope(Set<String> scope);

    @NotNull
    String getGrantType();

    void setGrantType(String grantType);
}