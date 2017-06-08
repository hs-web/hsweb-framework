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
package org.hswebframework.web.authorization.oauth2.client;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 默认的服务实现
 *
 * @author zhouhao
 */
public class AccessTokenInfo {
    //授权码
    @JSONField(name = "access_token")
    private String  accessToken;
    //更新码
    @JSONField(name = "refresh_token")
    private String  refreshToken;
    //有效期
    @JSONField(name = "expires_in")
    private Integer expiresIn;
    //授权范围
    private String  scope;

    private Long createTime;

    private Long updateTime;

    @JSONField(name = "token_type")
    private String tokenType;

    public boolean isExpire() {
        return updateTime != null && System.currentTimeMillis() - updateTime > expiresIn * 1000;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * @return 授权码
     */
    public String getAccessToken() {
        return this.accessToken;
    }

    /**
     * 设置 授权码
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * @return 更新码
     */
    public String getRefreshToken() {
        return this.refreshToken;
    }

    /**
     * 设置 更新码
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * @return 有效期
     */
    public Integer getExpiresIn() {
        return this.expiresIn;
    }

    /**
     * 设置 有效期
     */
    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    /**
     * @return 授权范围
     */
    public String getScope() {
        return this.scope;
    }

    /**
     * 设置 授权范围
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
