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
 */
package org.hswebframework.web.entity.oauth2.client;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * OAuth2用户授权信息
 *
 * @author hsweb-generator-online
 */
public class SimpleOAuth2UserTokenEntity extends SimpleGenericEntity<String> implements OAuth2UserTokenEntity {
    //客户端用户id
    private String  clientUserId;
    //服务端用户id
    private String  serverUserId;
    //服务端id
    private String  serverId;
    //客户端id
    private String  clientId;
    //授权码
    private String  accessToken;
    //更新码
    private String  refreshToken;
    //有效期
    private Integer expiresIn;
    //授权范围
    private String  scope;
    //创建时间
    private Long    createTime;
    //更新时间
    private Long    updateTime;

    private String grantType;

    @Override
    public String getGrantType() {
        return grantType;
    }

    @Override
    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    /**
     * @return 客户端用户id
     */
    @Override
    public String getClientUserId() {
        return this.clientUserId;
    }

    /**
     * 设置 客户端用户id
     */
    @Override
    public void setClientUserId(String clientUserId) {
        this.clientUserId = clientUserId;
    }

    /**
     * @return 服务端用户id
     */
    @Override
    public String getServerUserId() {
        return this.serverUserId;
    }

    /**
     * 设置 服务端用户id
     */
    @Override
    public void setServerUserId(String serverUserId) {
        this.serverUserId = serverUserId;
    }

    /**
     * @return 服务端id
     */
    @Override
    public String getServerId() {
        return this.serverId;
    }

    /**
     * 设置 服务端id
     */
    @Override
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * @return 客户端id
     */
    @Override
    public String getClientId() {
        return this.clientId;
    }

    /**
     * 设置 客户端id
     */
    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @return 授权码
     */
    @Override
    public String getAccessToken() {
        return this.accessToken;
    }

    /**
     * 设置 授权码
     */
    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * @return 更新码
     */
    @Override
    public String getRefreshToken() {
        return this.refreshToken;
    }

    /**
     * 设置 更新码
     */
    @Override
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * @return 有效期
     */
    @Override
    public Integer getExpiresIn() {
        return this.expiresIn;
    }

    /**
     * 设置 有效期
     */
    @Override
    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    /**
     * @return 授权范围
     */
    @Override
    public String getScope() {
        return this.scope;
    }

    /**
     * 设置 授权范围
     */
    @Override
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * @return 创建时间
     */
    @Override
    public Long getCreateTime() {
        return this.createTime;
    }

    /**
     * 设置 创建时间
     */
    @Override
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * @return 更新时间
     */
    @Override
    public Long getUpdateTime() {
        return this.updateTime;
    }

    /**
     * 设置 更新时间
     */
    @Override
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}