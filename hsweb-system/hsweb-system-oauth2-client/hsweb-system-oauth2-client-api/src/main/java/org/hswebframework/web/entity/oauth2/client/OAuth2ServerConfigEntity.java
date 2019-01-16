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

import org.hswebframework.web.commons.entity.GenericEntity;

/**
 * OAuth2服务配置 实体
 *
 * @author hsweb-generator-online
 */
public interface OAuth2ServerConfigEntity extends GenericEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 服务名称
     */
    String name           = "name";
    /**
     * 备注
     */
    String describe       = "describe";
    /**
     * api根地址
     */
    String apiBaseUrl     = "apiBaseUrl";
    /**
     * 认证地址
     */
    String authUrl        = "authUrl";
    /**
     * token获取地址
     */
    String accessTokenUrl = "accessTokenUrl";
    /**
     * 客户端id
     */
    String clientId       = "clientId";
    /**
     * 客户端密钥
     */
    String clientSecret   = "clientSecret";
    /**
     * 状态
     */
    String status         = "status";

    String redirectUri = "redirectUri";

    String provider = "provider";

    String getProvider();

    void setProvider(String provider);

    String getRedirectUri();

    void setRedirectUri(String redirectUri);

    /**
     * @return 服务名称
     */
    String getName();

    /**
     * 设置 服务名称
     */
    void setName(String name);

    /**
     * @return 备注
     */
    String getDescribe();

    /**
     * 设置 备注
     */
    void setDescribe(String describe);

    /**
     * @return api根地址
     */
    String getApiBaseUrl();

    /**
     * 设置 api根地址
     */
    void setApiBaseUrl(String apiBaseUrl);

    /**
     * @return 认证地址
     */
    String getAuthUrl();

    default String getRealUrl(String url) {
        String base = getApiBaseUrl();
        if (url.startsWith("http")) {
            return url;
        }
        if (!base.endsWith("/") && !url.startsWith("/")) {
            base += "/";
        }
        return base + url;
    }

    /**
     * 设置 认证地址
     */
    void setAuthUrl(String authUrl);

    /**
     * @return token获取地址
     */
    String getAccessTokenUrl();

    /**
     * @param accessTokenUrl token获取地址
     */
    void setAccessTokenUrl(String accessTokenUrl);

    /**
     * @return 客户端id
     */
    String getClientId();

    /**
     * 设置 客户端id
     */
    void setClientId(String clientId);

    /**
     * @return 客户端密钥
     */
    String getClientSecret();

    /**
     * 设置 客户端密钥
     */
    void setClientSecret(String clientSecret);

    /**
     * @return 是否启用
     */
    Byte getStatus();

    /**
     * 设置 是否启用
     */
    void setStatus(Byte status);

}