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
package org.hswebframework.web.entity.oauth2.client;

import org.hswebframework.web.commons.entity.GenericEntity;

/**
 * OAuth2用户授权信息 实体
 *
 * @author hsweb-generator-online
 */
public interface OAuth2UserTokenEntity extends GenericEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 客户端用户id
     */
    String clientUserId = "clientUserId";
    /**
     * 服务端用户id
     */
    String serverUserId = "serverUserId";
    /**
     * 服务端id
     */
    String serverId     = "serverId";
    /**
     * 客户端id
     */
    String clientId     = "clientId";
    /**
     * 授权码
     */
    String accessToken  = "accessToken";
    /**
     * 更新码
     */
    String refreshToken = "refreshToken";
    /**
     * 有效期
     */
    String expireIn     = "expireIn";
    /**
     * 授权范围
     */
    String scope        = "scope";
    /**
     * 创建时间
     */
    String createTime   = "createTime";
    /**
     * 更新时间
     */
    String updateTime   = "updateTime";

    //授权方式
    String grantType = "grantType";

    /**
     * @return 授权方式
     */
    String getGrantType();

    /**
     * 设置 授权方式
     */
    void setGrantType(String grantType);

    /**
     * @return 客户端用户id
     */
    String getClientUserId();

    /**
     * 设置 客户端用户id
     */
    void setClientUserId(String clientUserId);

    /**
     * @return 服务端用户id
     */
    String getServerUserId();

    /**
     * 设置 服务端用户id
     */
    void setServerUserId(String serverUserId);

    /**
     * @return 服务端id
     */
    String getServerId();

    /**
     * 设置 服务端id
     */
    void setServerId(String serverId);

    /**
     * @return 客户端id
     */
    String getClientId();

    /**
     * 设置 客户端id
     */
    void setClientId(String clientId);

    /**
     * @return 授权码
     */
    String getAccessToken();

    /**
     * 设置 授权码
     */
    void setAccessToken(String accessToken);

    /**
     * @return 更新码
     */
    String getRefreshToken();

    /**
     * 设置 更新码
     */
    void setRefreshToken(String refreshToken);

    /**
     * @return 有效期
     */
    Integer getExpiresIn();

    /**
     * 设置 有效期
     */
    void setExpiresIn(Integer expiresIn);

    /**
     * @return 授权范围
     */
    String getScope();

    /**
     * 设置 授权范围
     */
    void setScope(String scope);

    /**
     * @return 创建时间
     */
    Long getCreateTime();

    /**
     * 设置 创建时间
     */
    void setCreateTime(Long createTime);

    /**
     * @return 更新时间
     */
    Long getUpdateTime();

    /**
     * 设置 更新时间
     */
    void setUpdateTime(Long updateTime);

}