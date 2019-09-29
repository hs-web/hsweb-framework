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

import lombok.*;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.JDBCType;
import java.util.Set;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "s_oauth2_client")
public class SimpleOAuth2ClientEntity extends SimpleGenericEntity<String> implements OAuth2ClientEntity {
    private static final long serialVersionUID = -8370400980996896599L;
    @Override
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }
    @Column
    private String name;

    @Column
    private String secret;

    @Column(name = "redirect_uri")
    private String redirectUri;

    @Column(name = "owner_id")
    private String ownerId;

    @Column(name = "creator_id")
    private String creatorId;

    @Column(name = "create_time")
    private Long createTime;

    @Column
    private String type;

    @Column
    private String describe;

    @Column(name = "support_grant_types")
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    private Set<String> supportGrantTypes;

    @Column(name = "default_grant_scope")
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    private Set<String> defaultGrantScope;

    @Column
    private Byte status;

}
