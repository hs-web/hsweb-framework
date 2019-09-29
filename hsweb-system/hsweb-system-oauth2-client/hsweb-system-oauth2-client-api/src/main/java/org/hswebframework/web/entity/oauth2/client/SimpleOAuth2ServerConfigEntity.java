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

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * OAuth2服务配置
 *
 * @author hsweb-generator-online
 */
@Table(name = "s_oauth2_server")
@Getter
@Setter
public class SimpleOAuth2ServerConfigEntity extends SimpleGenericEntity<String> implements OAuth2ServerConfigEntity {
    //服务名称
    @Column
    private String name;
    //备注
    @Column
    private String describe;
    //api根地址
    @Column(name = "api_base_url",length = 1024)
    private String apiBaseUrl;
    //认证地址
    @Column(name = "auth_url",length = 1024)
    private String authUrl;
    //token获取地址
    @Column(name = "access_token_url",length = 1024)
    private String accessTokenUrl;
    //客户端id
    @Column(name = "client_id",length = 128)
    private String clientId;
    //客户端密钥
    @Column(name = "client_secret",length = 128)
    private String clientSecret;
    //是否启用
    @Column
    private Byte   status;
    //重定向地址
    @Column(name = "redirect_uri",length = 1024)
    private String redirectUri;

    //服务提供商
    @Column
    private String provider;

    @Override
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }
}