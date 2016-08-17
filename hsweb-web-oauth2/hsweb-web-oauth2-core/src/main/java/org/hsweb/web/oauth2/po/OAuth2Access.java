/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hsweb.web.oauth2.po;

import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.bean.po.user.User;

/**
 * OAuth2认证信息
 * Created by hsweb-generator Aug 16, 2016 9:27:59 AM
 */
public class OAuth2Access extends GenericPo<String> {
    //客户端ID
    private String clientId;
    //关联用户
    private String userId;
    //认证码
    private String accessToken;
    //刷新码
    private String refreshToken;
    //过期时间
    private int expireIn;
    //生成日期
    private java.util.Date createDate;

    //关联用户实体
    private User user;

    /**
     * 获取 客户端ID
     *
     * @return String 客户端ID
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * 设置 客户端ID
     *
     * @param clientId 客户端ID
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * 获取 关联用户
     *
     * @return String 关联用户
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * 设置 关联用户
     *
     * @param userId 关联用户
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取 认证码
     *
     * @return String 认证码
     */
    public String getAccessToken() {
        return this.accessToken;
    }

    /**
     * 设置 认证码
     *
     * @param accessToken 认证码
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }


    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * 获取 过期时间
     *
     * @return int 过期时间
     */
    public int getExpireIn() {
        return this.expireIn;
    }

    /**
     * 设置 过期时间
     *
     * @param expireIn 过期时间
     */
    public void setExpireIn(int expireIn) {
        this.expireIn = expireIn;
    }

    /**
     * 获取 生成日期
     *
     * @return java.util.Date 生成日期
     */
    public java.util.Date getCreateDate() {
        return this.createDate;
    }

    /**
     * 设置 生成日期
     *
     * @param createDate 生成日期
     */
    public void setCreateDate(java.util.Date createDate) {
        this.createDate = createDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getLeftTime() {
        return getExpireIn() - (System.currentTimeMillis() - getCreateDate().getTime()) / 1000;
    }
}