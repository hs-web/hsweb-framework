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

import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.sql.JDBCType;

/**
 * OAuth2用户授权信息
 *
 * @author hsweb-generator-online
 */
@Table(name = "s_oauth2_user_token")
public class SimpleOAuth2UserTokenEntity extends SimpleGenericEntity<String> implements OAuth2UserTokenEntity {
    //客户端用户id
    @Column(name = "client_user_id", length = 128)
    private String clientUserId;
    //服务端用户id
    @Column(name = "server_user_id", length = 32)
    private String serverUserId;
    //服务端id
    @Column(name = "server_id")
    private String serverId;
    //客户端id
    @Column(name = "client_id")
    private String clientId;
    //授权码
    @Column(name = "access_token")
    private String accessToken;
    //更新码
    @Column(name = "refresh_token")
    private String refreshToken;
    //有效期
    @Column(name = "expires_in")
    private Integer expiresIn;
    //授权范围
    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    private String scope;
    //创建时间
    @Column(name = "create_time")
    private Long createTime;
    //更新时间
    @Column(name = "update_time")
    private Long updateTime;

    @Column(name = "grant_type")
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