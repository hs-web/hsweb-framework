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

package org.hswebframework.web.entity.oauth2.server;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.web.commons.entity.annotation.ImplementFor;

import javax.persistence.Column;
import javax.persistence.Table;
import java.sql.JDBCType;
import java.util.Set;

/**
 *
 * @author zhouhao
 */
@Getter
@Setter
@Table(name = "s_oauth2_access")
@ImplementFor(OAuth2AccessEntity.class)
public class SimpleOAuth2AccessEntity implements OAuth2AccessEntity {

    private static final long serialVersionUID = 2090466474249489203L;
    @Column(name = "client_id",length = 32)
    private String clientId;

    @Column(name = "owner_id",length = 32)
    private String ownerId;

    @Column(name = "access_token",length = 64)
    private String accessToken;

    @Column(name = "refresh_token",length = 64)
    private String refreshToken;

    @Column(name = "expires_int")
    private Integer expiresIn;

    @Column(name = "create_time")
    private Long createTime;

    @Column(name = "update_time")
    private Long updateTime;

    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    private Set<String> scope;

    @Column(name = "grant_type")
    private String grantType;

}
