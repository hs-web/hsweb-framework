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
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.JDBCType;

/**
 * OAuth2用户授权信息
 *
 * @author hsweb-generator-online
 */
@Table(name = "s_oauth2_user_token")
@Getter
@Setter
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
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }
}