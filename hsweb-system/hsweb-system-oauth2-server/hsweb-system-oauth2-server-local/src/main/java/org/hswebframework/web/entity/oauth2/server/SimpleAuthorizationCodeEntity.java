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
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.commons.entity.annotation.ImplementFor;

import javax.persistence.Column;
import javax.persistence.Table;
import java.sql.JDBCType;
import java.util.Set;

/**
 * @author zhouhao
 */
@Getter
@Setter
@Table(name = "s_oauth2_auth_code")
@ImplementFor(AuthorizationCodeEntity.class)
public class SimpleAuthorizationCodeEntity implements AuthorizationCodeEntity {

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "user_id")
    private String userId;

    @Column
    private String code;

    @Column(name = "create_time")
    private Long createTime;

    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    @JsonCodec
    private Set<String> scope;

    @Column(name = "redirect_uri", length = 1024)
    private String redirectUri;


}
